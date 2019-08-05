package simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ReconnectionManager.ReconnectionPolicy;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.Roster.SubscriptionMode;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import com.google.gson.Gson;

import messages.server.Server;

import javax.net.ssl.SSLContext;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.SecureRandom;


public class DummyOptimizationTool {
	private static final String RESOURCE = "cpswarm";
	private XMPPTCPConnection connection;
	private Server server;
	private boolean available = true;
	private boolean started = false;
	private OptimizationConnectionListenerImpl connectionListener;
	private StanzaListener packetListener;
	//private RosterListener rosterListener;
	private Jid clientJID = null;
	private String serverName = null;
	private String clientID = null;
	private String optimizationID = null;
	private Jid orchestratorJid = null;
	private String SCID = null;
	private String simulationID = null;
	private String otDataFolder = null;
	private String optimizationConfiguration = null;
	private List<EntityFullJid> managers = new ArrayList<EntityFullJid>();
	private OptimizationMessageEventCoordinatorImpl messageListener = null;
	
	public DummyOptimizationTool(String clientID, final InetAddress serverIP, final String serverName, final String serverPassword, String dataFolder) {
		this.clientID = clientID;
		this.serverName = serverName;
		if(!dataFolder.endsWith(File.separator)) {
			dataFolder+=File.separator;
		}
		this.otDataFolder = dataFolder;
		if(!otDataFolder.endsWith(File.separator)) {
			otDataFolder+=File.separator;
		}
		try {

			clientJID = JidCreate.from(clientID+"@"+serverName+"/"+RESOURCE);
			orchestratorJid = JidCreate.from("orchestrator@"+serverName+"/"+RESOURCE);
			final SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, null, new SecureRandom());

			XMPPTCPConnectionConfiguration connectionConfig = XMPPTCPConnectionConfiguration
					.builder().setHostAddress(serverIP).setPort(5222)
					.setXmppDomain(serverName)
					.setCompressionEnabled(false).setCustomSSLContext(sc)
					.build();
			connection = new XMPPTCPConnection(connectionConfig);
			connection.connect();

			System.out.println("Connected to server");

			connectionListener = new OptimizationConnectionListenerImpl(this);
			// Adds a listener for the status of the connection
			connection.addConnectionListener(connectionListener);
/*
 * Removed because the connection has not to be automatically reconnected to test the OT recovery
 * 
			ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
			reconnectionManager.enableAutomaticReconnection();
			reconnectionManager.setReconnectionPolicy(ReconnectionPolicy.RANDOM_INCREASING_DELAY);
*/		
			// Adds the packet listener, used to catch the requests
			// of adding this client to the roster
			final StanzaFilter presenceFilter = new StanzaTypeFilter(
					Presence.class);
			packetListener = new OptimizationPresencePacketListener(this, ManagerMessageEventCoordinatorImpl.class);

			addAsyncStanzaListener(packetListener, presenceFilter);

			// Adds the listener for the incoming messages
			messageListener  = new OptimizationMessageEventCoordinatorImpl(this);
			ChatManager.getInstanceFor(connection).addIncomingListener(messageListener);
			
			connection.login(clientID, serverPassword , Resourcepart.from(RESOURCE));
			Thread.sleep(2000);
		} catch (final SmackException | IOException | XMPPException e) {
			if (e instanceof SASLErrorException) {
				connection.disconnect();
				createAccount(serverPassword);
			}
		} catch(Exception me) {
			System.out.println("msg "+me.getMessage());
			System.out.println("loc "+me.getLocalizedMessage());
			System.out.println("cause "+me.getCause());
			System.out.println("excep "+me);
			me.printStackTrace();
		}
		
		addOrchestratorToTheRoster();
	}

    
    private boolean createAccount(final String password) {
		final AccountManager accountManager = AccountManager
				.getInstance(connection);
		final HashMap<String, String> props = new HashMap<String, String>();
		// The description will be the property name of the account
		props.put("name", "optimization tool");
		Localpart part;
		try {
			part = Localpart.from(clientID);
			connection.connect();
			accountManager.createAccount(part, password, props);
			connection.login(clientID, password, Resourcepart.from(RESOURCE));
		} catch (InterruptedException | SmackException | IOException | XMPPException me) {
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
			return false;
		}
		return true;
    }
    

	/**
	 * This method verifies if the receiver supports the file transfer and in this
	 * case it sends a file
	 */
	public boolean transferFile(final EntityFullJid receiver, String fileToTransfer, final String message) {
		final ServiceDiscoveryManager disco = ServiceDiscoveryManager.getInstanceFor(connection);
		System.out.println("OT transfers file: "+fileToTransfer +"  to "+ receiver +" with message = " + message);
		// Receives the info about the client of the receiver
		DiscoverInfo discoInfo = null;
		try {
			discoInfo = disco.discoverInfo(receiver);
		} catch (XMPPException | NoResponseException | NotConnectedException | InterruptedException e) {
			e.printStackTrace();
		}

		// Controls if the file transfer is supported
		if (discoInfo.containsFeature("http://jabber.org/protocol/si/profile/file-transfer")) {
			final FileTransferManager manager = FileTransferManager.getInstanceFor(connection);
			OutgoingFileTransfer transfer = null;
			transfer = manager.createOutgoingFileTransfer(receiver);
			// Here the file is actually sent
			try {
				transfer.sendFile(new File(fileToTransfer), message);
				System.out.println("File sent, waiting transfer complete");
				while (!transfer.isDone() && transfer.getException() == null) {
					if (transfer.getStatus() == Status.refused) {
						System.out.println("Transfer refused");
					}
					Thread.sleep(1000);
				}
			} catch (final SmackException | InterruptedException e) {
				e.printStackTrace();
			}
			Exception ex = transfer.getException();
			if(ex!=null) {
				System.out.println("Exception transferring file "+ex.getMessage());
				return false;
			}

			switch (transfer.getStatus()) {
			case cancelled:
				System.out.println("Transfer cancelled");
				return false;
			case error:
				System.out.println("Error in file transfer" + transfer.getError());
				return false;
			case complete:
				System.out.println("File transfered");
				return true;
			default:
				System.out.println("Transfer not completed");
				return false;
			}
		} else {
			System.out.println("Error, the Simulation manager: " + receiver + " doesn't support the file transfer");
			return false;
		}
	}
	
	
	public boolean sendOptimizationState() {
		
		// TODO the state file called SCID will be saved in the subfolder named with OID in the otDataFolder
		String stateFile = this.otDataFolder + this.optimizationID + File.separator + this.SCID;
		File file = new File(stateFile);
		if (file.exists()) {
			file.delete();
		}else {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		try {			
			if (!this.transferFile(
					JidCreate.entityFullFrom(
							this.orchestratorJid.toString()), stateFile, this.optimizationID)) {
				return false;
			}
		} catch (XmppStringprepException e) {
				e.printStackTrace();
				return false;
		}	
		return true;
	}
	
    
	/**
	 * Method used to add a {@link RosterListener} to the roster
	 *
	 * @param listener
	 *            the listener that will receive the notification
	 *
	 * @return a <code>boolean</code>: true if all is ok, otherwise false
	 *
	 * @throws AsserionError
	 *             if something is wrong
	 *
	 */
	private boolean addRosterListener(final RosterListener listener) {
		try {
			final Roster roster = Roster.getInstanceFor(connection);
			roster.addRosterListener(listener);
			return true;
		} catch (final IllegalStateException e) {
			// The client is disconnected
			System.out.println(
					"Connection disconnected, listener addition interrupted");
			return false;
		}
	}
	
	/**
	 * Method used to add a {@link PacketListener</code> to the connection
	 *
	 * @param listener
	 *            the listener that will receive the notification
	 *
	 * @return a <code>boolean</code>: true if all is ok, otherwise false
	 *
	 *
	 * @throws AsserionError
	 *             if something is wrong
	 *
	 *
	 */
	private boolean addAsyncStanzaListener(final StanzaListener listener,
			final StanzaFilter filter) {
		try {
			connection.addAsyncStanzaListener(listener, filter);
			return true;
			// The client is disconnected
		} catch (final IllegalStateException e) {
			System.out.println(
					"Connection disconnected, packet listener addition interrupted");
			return false;
		}
	}
    

	/**
	 * Method used to add to the roster the Orchestrator and the Optimization Tool
	 *
	 * @throws XMPPException
	 *             if something is wrong
	 */
	private void addOrchestratorToTheRoster() {
		// Sets the type of subscription of the roster
		final Roster roster = Roster.getInstanceFor(connection);
		roster.setSubscriptionMode(SubscriptionMode.accept_all);
		try {
			final String[] groups = { "orchestrator" };
			RosterGroup group = roster
				.getGroup("orchestrator");		
			if (group != null) {
				if (!group.contains(JidCreate.bareFrom("orchestrator@"
						+ serverName))) {
					roster.createEntry(JidCreate.bareFrom("orchestrator@"
							+ serverName),
							"orchestrator", groups);
				} 
			} else {
				roster.createEntry(JidCreate.bareFrom("orchestrator@"
						+ serverName),
						"orchestrator", groups);
			}
		} catch (XmppStringprepException | NotLoggedInException | NoResponseException | XMPPErrorException
				| NotConnectedException | InterruptedException e) {
			// The client is disconnected
			System.out.println(
					"Connection disconnected, adding system bundles to roster interrupted");
			e.printStackTrace();
		} 
	}
	
		
	public void setServerInfo(Server serverInfo) {
		server = serverInfo;
	}
	
	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean availalble) {
		this.available = availalble;
	}
	
	
	public void disconnect() {
		this.connection.disconnect();
	}
	
	public void reconnect() {
		try {
			this.connection.connect();
		} catch (SmackException | IOException | XMPPException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public boolean publishServer(String simulationHash) {
		try {
			Gson gson = new Gson();
			String serverString = gson.toJson(server, Server.class); 
			PubSubManager manager = PubSubManager.getInstance(connection);
        	LeafNode node = manager.getLeafNode("server");
        	node.publish(new Item(serverString));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	};

	public boolean isStarted() {
		return started;
	}
	
	public String getOptimizationID() {
		return optimizationID;
	}


	public void setOptimizationID(String optimizationID) {
		this.optimizationID = optimizationID;
	}


	public String getSimulationID() {
		return simulationID;
	}
	
	public String getSCID() {
		return SCID;
	}

	public void setSCID(final String SCID) {
		this.SCID = SCID;
	}

	public void setSimulationID(String simulationID) {
		this.simulationID = simulationID;
	}
	
	public String getOtDataFolder() {
		return this.otDataFolder;
	}

	public Server getServer() {
		return server;
	};
	
	public XMPPTCPConnection getConnection() {
		return connection;
	}
	
	public Jid getJid() {
		return clientJID;
	}

	public Jid getOrchestratorJid() {
		return orchestratorJid;
	}


	public String getOptimizationConfiguration() {
		return optimizationConfiguration;
	}

	public void setOptimizationConfiguration(String optimizationConfiguration) {
		this.optimizationConfiguration = optimizationConfiguration;
	}
	
	public String getServerName() {
		return serverName;
	}


	public List<EntityFullJid> getManagers() {
		return managers;
	}


	public void setManagers(List<String> managers) {
		for(String manager : managers) {
			try {
				this.managers.add(JidCreate.entityFullFrom(manager+"/"+RESOURCE));
			} catch (XmppStringprepException e) {
				System.out.println("Invalid username for the manager: "+manager);
			}
		}
	}
	
	public void setManager(final EntityFullJid jid) {
		this.managers.add(jid);
	}
	
	public void removeManager(final EntityFullJid jid) {
		managers.remove(jid);
	}
	
}