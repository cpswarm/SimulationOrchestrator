package simulation;

import java.util.HashMap;
import java.util.UUID;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
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
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import com.google.gson.Gson;

import messages.server.Server;

import javax.net.ssl.SSLContext;

import java.io.IOException;
import java.security.SecureRandom;


public class DummyOptimizationTool {
	private static final String RESOURCE = "cpswarm";
	private XMPPTCPConnection connection;
	private Server server;
	private boolean available = true;
	private boolean started = false;
	private String simulationHash="";
	private OptimizationConnectionListenerImpl connectionListener;
	private StanzaListener packetListener;
	//private RosterListener rosterListener;
	private Jid clientJID = null;
	private String serverName = null;
	private String clientID = null;
	private String simulationID = null;
	private Boolean guiEnabled = false;
	
	public DummyOptimizationTool(final String serverIP, final String serverName, final String serverPassword, String dataFolder, final String optimizationId ) {
		clientID = "optimization_test";
		this.simulationID = optimizationId;
		this.serverName = serverName;
		if(!dataFolder.endsWith("\\") && OsUtils.isWindows()) {
			dataFolder+="\\";
		} else if (!dataFolder.endsWith("/") && !OsUtils.isWindows()) {
			dataFolder+="/";
		}
		
		try {

			clientJID = JidCreate.from(clientID+"@"+serverName);
			final SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, null, new SecureRandom());

			XMPPTCPConnectionConfiguration connectionConfig = XMPPTCPConnectionConfiguration
					.builder().setHost(serverIP).setPort(5222)
					.setXmppDomain(serverName)
					.setCompressionEnabled(false).setCustomSSLContext(sc)
					.setDebuggerEnabled(true).build();
			connection = new XMPPTCPConnection(connectionConfig);
			connection.connect();

			System.out.println("Connected to server");

			connectionListener = new OptimizationConnectionListenerImpl(this);
			// Adds a listener for the status of the connection
			connection.addConnectionListener(connectionListener);

			final FileTransferManager manager = FileTransferManager
					.getInstanceFor(connection);
			
			manager.addFileTransferListener(new OptimizationFileTransferListenerImpl(this, dataFolder, JidCreate.entityBareFrom("orchestrator@"+serverName+"/"+RESOURCE), JidCreate.entityFullFrom("manager_test@"+serverName+"/"+RESOURCE)));
			
			//rosterListener = new RosterListenerImpl(this);
			// Adds a roster listener
			//addRosterListener(rosterListener);

			// Adds the packet listener, used to catch the requests
			// of adding this client to the roster
			final StanzaFilter presenceFilter = new StanzaTypeFilter(
					Presence.class);
			packetListener = new OptimizationPresencePacketListener(this, ManagerMessageEventCoordinatorImpl.class);

			addAsyncStanzaListener(packetListener, presenceFilter);

			// Adds the listener for the incoming messages
			ChatManager.getInstanceFor(connection).addIncomingListener(new OptimizationMessageEventCoordinatorImpl(this));
			
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
		props.put("name", "server");
		Localpart part;
		try {
			part = Localpart.from(clientID);
			connection.connect();
			accountManager.createAccount(part, password, props);
			connection.login(clientID, password, Resourcepart.from(RESOURCE));
			Thread.sleep(2000);
			final Presence presence = new Presence(Presence.Type.available);
			presence.setStatus("{\r\n" + 
					"	\"server\": 1,\r\n" + 
					"	\"simulation_hash\": \"21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4\",\r\n" + 
					"	\"simulations\": [\"minisim\"],\r\n" + 
					"	\"capabilities\": {\r\n" + 
					"		\"dimensions\": 2\r\n" + 
					"	}\r\n" + 
					"}\r\n" + 
					"");
			try {
				connection.sendStanza(presence);
			} catch (final NotConnectedException | InterruptedException e) {
				e.printStackTrace();
			}
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
	
		
	public boolean publishServer(String simulationHash) {
		try {
			Gson gson = new Gson();
			server.setSimulationHash(simulationHash);
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

	public String getSimulationID() {
		return simulationID;
	}


	public void setSimulationID(String simulationID) {
		this.simulationID = simulationID;
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


	public Boolean getGuiEnabled() {
		return guiEnabled;
	}


	public void setGuiEnabled(Boolean guiEnabled) {
		this.guiEnabled = guiEnabled;
	}
}