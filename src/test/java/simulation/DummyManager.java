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
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
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

import eu.cpswarm.optimization.messages.MessageSerializer;
import eu.cpswarm.optimization.messages.OptimizationCompleteMessage;
import eu.cpswarm.optimization.messages.OptimizationReplyMessage.Status;
import messages.server.Server;

import javax.net.ssl.SSLContext;

import java.io.IOException;
import java.security.SecureRandom;


public class DummyManager {
	private static final String RESOURCE = "cpswarm";
	private XMPPTCPConnection connection;
	private Server server;
	private boolean available = true;
	private boolean started = false;
	private ManagerConnectionListenerImpl connectionListener;
	private StanzaListener packetListener;
	//private RosterListener rosterListener;
	private Jid clientJID = null;
	private String serverName = null;
	private String clientID = null;
	private String optimizationId = null;
	private Boolean guiEnabled = false;
	private String params = null;
	private Boolean simulationDone = null;
	
	public DummyManager(final String clientID, final String serverIP, final String serverName, final String serverPassword, String dataFolder, final String rosFolder, final String optimizationId) {
		this.clientID = clientID;
		this.optimizationId = optimizationId;
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

			connectionListener = new ManagerConnectionListenerImpl(this);
			// Adds a listener for the status of the connection
			connection.addConnectionListener(connectionListener);

			final FileTransferManager manager = FileTransferManager
					.getInstanceFor(connection);
			
			manager.addFileTransferListener(new ManagerFileTransferListenerImpl(this, dataFolder, rosFolder, JidCreate.entityBareFrom("orchestrator@"+serverName+"/"+RESOURCE), optimizationId));
			
			//rosterListener = new RosterListenerImpl(this);
			// Adds a roster listener
			//addRosterListener(rosterListener);

			// Adds the packet listener, used to catch the requests
			// of adding this client to the roster
			final StanzaFilter presenceFilter = new StanzaTypeFilter(
					Presence.class);
			packetListener = new ManagerPresencePacketListener(this, ManagerMessageEventCoordinatorImpl.class);

			addAsyncStanzaListener(packetListener, presenceFilter);

			// Adds the listener for the incoming messages
			ChatManager.getInstanceFor(connection).addIncomingListener(new ManagerMessageEventCoordinatorImpl(this));
			
			connection.login(clientID, serverPassword , Resourcepart.from(RESOURCE));
			do {
				Thread.sleep(1000);
			}while(!connection.isConnected() || !connection.isAuthenticated());
			final Presence presence = new Presence(Presence.Type.available);
			presence.setStatus("{\r\n" + 
				   "	\"server\": 1,\r\n" + 
				   "	\"simulation_hash\": \"21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4\",\r\n" + 
				   "	\"simulations\": [\"stage\"],\r\n" + 
				   "	\"capabilities\": {\r\n" + 
				   "		\"dimensions\": 2\r\n" + 
				   "	}\r\n" + 
				   "}\r\n");
			try {
				connection.sendStanza(presence);
			} catch (final NotConnectedException | InterruptedException e) {
				e.printStackTrace();
			}
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
		addOrchestratorAndOptimizationToTheRoster();
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
			do {
				Thread.sleep(1000);
			}while(!connection.isConnected() || !connection.isAuthenticated());
			final Presence presence = new Presence(Presence.Type.available);
			presence.setStatus("{\r\n" + 
					"	\"server\": 1,\r\n" + 
					"	\"simulation_hash\": \"21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4\",\r\n" + 
					"	\"simulations\": [\"stage\"],\r\n" + 
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
	private void addOrchestratorAndOptimizationToTheRoster() {
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
			
			final String[] groups2 = { "optimization" };
			group = roster
				.getGroup("optimization");
			if (group != null) {
				if (!group.contains(JidCreate.bareFrom("optimization_test@"
						+ serverName))) {
					roster.createEntry(JidCreate.bareFrom("optimization_test@"
							+ serverName),
							"optimization", groups2);
				} 
			} else {
				roster.createEntry(JidCreate.bareFrom("optimization_test@"
						+ serverName),
						"optimization", groups2);
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
	
	public boolean publishFitness(final Double value) {
		OptimizationCompleteMessage result =  new OptimizationCompleteMessage(optimizationId, Status.OK, value.doubleValue());
		MessageSerializer serializer = new MessageSerializer();
		String body = serializer.toJson(result);
		try {
			System.out.println("Ready to send "+body);
			ChatManager chatManager = ChatManager.getInstanceFor(this.getConnection());
			Chat chat = chatManager.chatWith(JidCreate.entityBareFrom("optimization_test@"+this.serverName));
			Message message = new Message();
			message.setBody(body);
			chat.send(message);
			System.out.println("fitness score: "+ value + " sent");
		} catch (NotConnectedException | InterruptedException | XmppStringprepException e) {
			System.out.println("Error sending the result of the simulation: "+body);
			e.printStackTrace();
			return false;
		} 
		return true;
	}
	
	public boolean isStarted() {
		return started;
	}

	public String getOptimizationId() {
		return optimizationId;
	}


	public void setOptimizationID(final String optimizationId) {
		System.out.println("Setting Optimization ID to "+optimizationId);
		this.optimizationId = optimizationId;
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

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}


	public Boolean isSimulationDone() {
		return simulationDone;
	}

	public void setSimulationDone(boolean simulationDone) {
		System.out.println("Set simulation done");
		this.simulationDone = simulationDone;
	}	
}