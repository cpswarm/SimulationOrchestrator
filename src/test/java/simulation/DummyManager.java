package simulation;

import java.util.HashMap;

import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ReconnectionManager.ReconnectionPolicy;
import org.jivesoftware.smack.SmackConfiguration;
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
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import com.google.gson.Gson;

import eu.cpswarm.optimization.messages.MessageSerializer;
import eu.cpswarm.optimization.messages.SimulationResultMessage;
import eu.cpswarm.optimization.statuses.SimulationManagerCapabilities;
import eu.cpswarm.optimization.statuses.SimulationManagerStatus;
import eu.cpswarm.optimization.statuses.StatusSerializer;

import javax.net.ssl.SSLContext;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.SecureRandom;


public class DummyManager {
	private static final String RESOURCE = "cpswarm";
	private XMPPTCPConnection connection;
	private SimulationManagerStatus status;
	private boolean available = true;
	private boolean started = false;
	private boolean optimizationToolAvailable = false;
	private boolean orchestratorAvailable = false;
	private ManagerConnectionListenerImpl connectionListener;
	private StanzaListener packetListener;
	//private RosterListener rosterListener;
	private Jid clientJID = null;
	private Jid optimizationToolJID = null;
	private Jid orchestratorJID = null;
	private String serverName = null;
	private String clientID = null;
	private String optimizationId = null;
	private String SCID = null;
	private String simulationId = null;
	private String simulationConfiguration = null;
	private SimulationManagerCapabilities capabilities = null;
	
	public DummyManager(final String clientID, final InetAddress serverIP, final String serverName, final String serverPassword, String dataFolder, final String rosFolder) {
		this.clientID = clientID;
		this.serverName = serverName;
		capabilities = new SimulationManagerCapabilities(2, 8);
			
		try {

			clientJID = JidCreate.from(clientID+"@"+serverName);
			orchestratorJID = JidCreate.from("orchestrator_bamboo@" + serverName + "/"+RESOURCE);
			optimizationToolJID = JidCreate.from("optimization_bamboo@" + serverName + "/"+RESOURCE);
			final SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, null, new SecureRandom());
			SmackConfiguration.DEBUG = true;
			XMPPTCPConnectionConfiguration connectionConfig = XMPPTCPConnectionConfiguration
					.builder().setHostAddress(serverIP).setPort(5222)
					.setXmppDomain(serverName)
					.setCompressionEnabled(false).setCustomSSLContext(sc)
					.build();
			connection = new XMPPTCPConnection(connectionConfig);
			connection.connect();

			System.out.println("Connected to server");

			connectionListener = new ManagerConnectionListenerImpl(this);
			// Adds a listener for the status of the connection
			connection.addConnectionListener(connectionListener);

			ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
			reconnectionManager.enableAutomaticReconnection();
			reconnectionManager.setReconnectionPolicy(ReconnectionPolicy.RANDOM_INCREASING_DELAY);
			
			final FileTransferManager manager = FileTransferManager
					.getInstanceFor(connection);
			
			manager.addFileTransferListener(new ManagerFileTransferListenerImpl(this, dataFolder, rosFolder));
			
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
			SimulationManagerCapabilities caps = new SimulationManagerCapabilities(2,8);
			SimulationManagerStatus status = new SimulationManagerStatus("","", caps);
			StatusSerializer serializer = new StatusSerializer();
			presence.setStatus(serializer.toJson(status));
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
			SimulationManagerCapabilities caps = new SimulationManagerCapabilities(2,8);
			SimulationManagerStatus status = new SimulationManagerStatus("","", caps);
			StatusSerializer serializer = new StatusSerializer();
			presence.setStatus(serializer.toJson(status));
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
				if (!group.contains(orchestratorJID)) {
					roster.createEntry(orchestratorJID.asBareJid(),
							"orchestrator", groups);
				} 
			} else {
				roster.createEntry(orchestratorJID.asBareJid(),
						"orchestrator", groups);
			}
			
			final String[] groups2 = { "optimization" };
			group = roster
				.getGroup("optimization");

			if (group != null) {
				if (!group.contains(optimizationToolJID)) {
					roster.createEntry(optimizationToolJID.asBareJid(),
							"optimization", groups2);
				} 
			} else {
				roster.createEntry(optimizationToolJID.asBareJid(),
						"optimization", groups2);
			}			
			
		} catch (NotLoggedInException | NoResponseException | XMPPErrorException
				| NotConnectedException | InterruptedException e) {
			// The client is disconnected
			System.out.println(
					"Connection disconnected, adding system bundles to roster interrupted");
			e.printStackTrace();
		} 
	}
	
		
	public void setServerInfo(SimulationManagerStatus statusInfo) {
		status = statusInfo;
	}
	
	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean availalble) {
		this.available = availalble;
	}
	
			
	public boolean publishFitness(final Double value) {
		SimulationResultMessage result =  new SimulationResultMessage(optimizationId, true, simulationId, value.doubleValue());
		MessageSerializer serializer = new MessageSerializer();
		String body = serializer.toJson(result);
		try {
			ChatManager chatManager = ChatManager.getInstanceFor(this.getConnection());
			Chat chat = chatManager.chatWith(JidCreate.entityBareFrom("optimization_bamboo@"+this.serverName));
			Message message = new Message();
			message.setBody(body);
			if(isOptimizationToolAvailable()) {
				chat.send(message);
			}
			System.out.println("fitness score: "+ body + " sent");
		} catch (NotConnectedException | InterruptedException | XmppStringprepException e) {
			System.out.println("Error sending the result of the simulation: "+body);
			e.printStackTrace();
			return false;
		} 
		return true;
	}
	
	public void sendPresence() {
		final Presence presence = new Presence(Presence.Type.available);
		Gson gson = new Gson();
		SimulationManagerStatus simulationManagerStatus = new SimulationManagerStatus(SCID, simulationId, capabilities);
		presence.setStatus(gson.toJson(simulationManagerStatus));
		try {
			connection.sendStanza(presence);
		} catch (final NotConnectedException | InterruptedException e) {
			e.printStackTrace();
		}
		simulationManagerStatus =null;
	}
	
	public boolean isStarted() {
		return started;
	}

	public String getOptimizationId() {
		return optimizationId;
	}

	public void setOptimizationID(final String optimizationId) {
		this.optimizationId = optimizationId;
	}
	
	public String getSCID() {
		return SCID;
	}

	public void setSCID(final String SCID) {
		this.SCID = SCID;
		sendPresence();
	}

	public SimulationManagerStatus getStatus() {
		return status;
	};
	
	public XMPPTCPConnection getConnection() {
		return connection;
	}
	
	public Jid getJid() {
		return clientJID;
	}

	public Jid getOptimizationToolJID () {
		return optimizationToolJID;
	}
	
	public Jid getOrchestratorJID () {
		return orchestratorJID;
	}
	
	public String getSimulationConfiguration() {
		return simulationConfiguration;
	}


	public void setSimulationConfiguration(String simulationConfiguration) {
		this.simulationConfiguration = simulationConfiguration;
	}

	public String getSimulationId() {
		return simulationId;
	}

	public void setSimulationId(String simulationId) {
		this.simulationId = simulationId;
	}


	public boolean isOptimizationToolAvailable() {
		return optimizationToolAvailable;
	}

	public void setOptimizationToolAvailable(boolean optimizationToolAvailable) {
		this.optimizationToolAvailable = optimizationToolAvailable;
	}

	public boolean isOrchestratorAvailable() {
		return orchestratorAvailable;
	}

	public void setOrchestratorAvailable(boolean orchestratorAvailable) {
		this.orchestratorAvailable = orchestratorAvailable;
	}
}