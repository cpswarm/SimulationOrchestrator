package simulation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.roster.Roster.SubscriptionMode;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import config.Configentry;
import config.Configuration;
import config.Frevo;
import config.ObjectFactory;
import messages.server.Server;
import simulation.tools.Zipper;
import simulation.xmpp.ConnectionListenerImpl;
import simulation.xmpp.MessageEventCoordinatorImpl;
import simulation.xmpp.PacketListenerImpl;

import javax.net.ssl.SSLContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class SimulationOrchestrator {
	private static final String RESOURCE = "cpswarm";
	private XMPPTCPConnection connection;
	private ConnectionListenerImpl connectionListener;
	//private RosterListener rosterListener;
	private String serverName = null;
	private Map<EntityFullJid, Server> simulationManagers = null;
	private String dataFolder = null;
	private int managerConfigured = 0;
	private List<EntityFullJid> availableManagers = null;
	private String configurationFile = null;
	private Jid optimizationJid = null;
	private String simulationId = null;
	
	public static void main (String args[]) {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder;
		String serverURI = "";
		String serverName = "";
		String serverPassword = "";
		String dataFolder = "";
		String optimizationUser = "";
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(SimulationOrchestrator.class.getResourceAsStream("/orchestrator.xml"));
			serverURI = document.getElementsByTagName("serverURI").item(0).getTextContent();
			serverName = document.getElementsByTagName("serverName").item(0).getTextContent();
			serverPassword = document.getElementsByTagName("serverPassword").item(0).getTextContent();
			optimizationUser = document.getElementsByTagName("optimizationUser").item(0).getTextContent();
			dataFolder = document.getElementsByTagName("dataFolder").item(0).getTextContent();
			if(!dataFolder.endsWith("\\")) {
				dataFolder+="\\";
			}
			if(!new File(dataFolder).isDirectory()) {
				System.out.println("Data folder must be a folder");
				return;
			}
		} catch (ParserConfigurationException | SAXException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		} 
		new SimulationOrchestrator(serverURI, serverName, serverPassword, dataFolder, optimizationUser);
		while(true) {}
	}
	
	public SimulationOrchestrator(final String serverIP, final String serverName, final String serverPassword, final String dataFolder, final String optimizationUser) {
		this.serverName = serverName;
		this.dataFolder = dataFolder;
		this.simulationManagers = new HashMap<EntityFullJid, Server>();
		try {
			this.optimizationJid = JidCreate.from(optimizationUser+"@"+serverName+"/"+RESOURCE);

			final SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, null, new SecureRandom());

			XMPPTCPConnectionConfiguration connectionConfig = XMPPTCPConnectionConfiguration
					.builder().setHost(serverIP).setPort(5222)
					.setXmppDomain(serverName)
					.setCompressionEnabled(false).setCustomSSLContext(sc)
					.setDebuggerEnabled(true).build();
			connection = new XMPPTCPConnection(connectionConfig);
			
			final StanzaFilter presenceFilter = new StanzaTypeFilter(
					Presence.class);
			System.out.println("adding the packet listener to the local connection");
			// This listener checks the presences
			final PacketListenerImpl packetListener = new PacketListenerImpl(
					this);
			this.addAsyncStanzaListener(packetListener, presenceFilter);
			
			connection.connect();

			connection.login("orchestrator", serverPassword , Resourcepart.from(RESOURCE));
			System.out.println("Connected to server");

			connectionListener = new ConnectionListenerImpl(this);
			// Adds a listener for the status of the connection
			connection.addConnectionListener(connectionListener);

			// Adds the listener for the incoming messages
			ChatManager.getInstanceFor(connection).addIncomingListener(new MessageEventCoordinatorImpl(this));
			
			// Does the login
			connection.login(serverName, serverPassword, Resourcepart.from(RESOURCE));
			Thread.sleep(2000);
			final Presence presence = new Presence(Presence.Type.available);
			presence.setStatus("Pronto");
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
		
		addOptimizationToTheRoster();
	}

    
    private boolean createAccount(final String password) {
		final AccountManager accountManager = AccountManager
				.getInstance(connection);
		final HashMap<String, String> props = new HashMap<String, String>();
		// The description will be the property name of the account
		props.put("name", "server");
		Localpart part;
		try {
			part = Localpart.from("orchestrator");
			connection.connect();
			accountManager.createAccount(part, password, props);
			connection.login("orchestrator", password, Resourcepart.from(RESOURCE));
		} catch (InterruptedException | SmackException | IOException | XMPPException me) {
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
			return false;
		}
		return true;
    }
    
    public void evaluateSimulationManagers(Server serverCompare) {
    	Zipper zipper = new Zipper(dataFolder);
		configurationFile = zipper.generateFileList(new File(dataFolder));
    	String[] fileNameParts = (dataFolder+"test.zip").split("\\.");
    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss");
		Date date = new Date();
		String fileName = fileNameParts[0] + "_" + dateFormat.format(date) + "." + fileNameParts[1];
    	zipper.zipIt(fileName);
    	availableManagers = new ArrayList<EntityFullJid>();
    	simulationId = UUID.randomUUID().toString();
    	for(EntityFullJid account : simulationManagers.keySet()) {
    		if(simulationManagers.get(account).compareTo(serverCompare)>0) {
    			this.transferFile(account, fileName, simulationId);
    			availableManagers.add(account); 
    		}
    	}
    	modifyOptimizationToolConfiguration();
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
	
	
	private void modifyOptimizationToolConfiguration() {
		Frevo configuration = Configuration.loadConfFromXMLFile(new File(configurationFile), false);
		ObjectFactory factory = new ObjectFactory();
		Configentry configEntry= factory.createConfigentry();
		configEntry.setKey("threads");
		configEntry.setType("INT");
		configEntry.setValue(String.valueOf(availableManagers.size()));
		configuration.getSessionconfig().getConfigentry().add(configEntry);
		configEntry= factory.createConfigentry();
		configEntry.setKey("jids");
		configEntry.setType("STRING");
		configEntry.setValue(String.join(",", availableManagers));
		configuration.getSessionconfig().getConfigentry().add(configEntry);
		Configuration.storeConfInToXMLFile(new File(configurationFile), configuration, false);
	}
    	
	/**
	 * This method verifies if the receiver supports the file transfer and in
	 * this case it sends a file
	 */
	private void transferFile(final EntityFullJid receiver, final String filePath, final String message) {
		final ServiceDiscoveryManager disco = ServiceDiscoveryManager
				.getInstanceFor(connection);

		// Receives the info about the client of the receiver
		DiscoverInfo discoInfo = null;
		try {
			discoInfo = disco.discoverInfo(receiver);
		} catch (XMPPException | NoResponseException | NotConnectedException | InterruptedException e) {
			e.printStackTrace();
		}

		// Controls if the file transfer is supported
		if (discoInfo
				.containsFeature("http://jabber.org/protocol/si/profile/file-transfer")) {
			final FileTransferManager manager = FileTransferManager
					.getInstanceFor(connection);
			OutgoingFileTransfer transfer = null;
			transfer = manager
					.createOutgoingFileTransfer(receiver);
			// Here the file is actually sent
			try {
				transfer.sendFile(new File(filePath), message);
				while (!transfer.isDone()) {
					if (transfer.getStatus() == Status.refused) {
						System.out.println("Transfer refused");
					}
					Thread.sleep(1000);
				}
			} catch (final SmackException | InterruptedException e) {
				e.printStackTrace();
			}
			final Status status = transfer.getStatus();
			if (status == Status.cancelled) {
				System.out.println("Transfer cancelled");
			} else if (status == Status.error) {
				System.out.println("Error in file transfer");
			} else if (status == Status.complete) {
				System.out.println("File transferred");
			}
		}
	}
	
		
	/**
	 * Method used to add to the roster the Optimization Tool
	 *
	 * @throws XMPPException
	 *             if something is wrong
	 */
	private void addOptimizationToTheRoster() {
		// Sets the type of subscription of the roster
		final Roster roster = Roster.getInstanceFor(connection);
		roster.setSubscriptionMode(SubscriptionMode.accept_all);
		try {
			final String[] groups = { "optimization" };
			final RosterGroup group = roster
				.getGroup("optimization");
			if (group != null) {
				if (!group.contains(optimizationJid.asBareJid())) {
					roster.createEntry(optimizationJid.asBareJid(),
							"optimization", groups);
				} 
			} else {
				roster.createEntry(optimizationJid.asBareJid(),
						"optimization", groups);
			}			
			
		} catch (NotLoggedInException | NoResponseException | XMPPErrorException
				| NotConnectedException | InterruptedException e) {
			// The client is disconnected
			System.out.println(
					"Connection disconnected, adding system bundles to roster interrupted");
		} 
	}
	

	
	public XMPPTCPConnection getConnection() {
		return connection;
	}
	
	public void putSimulationManager(EntityFullJid jid, Server server) {
		simulationManagers.put(jid,server);
	}	

	public void removeSimulationManager(Jid jid) {
		simulationManagers.remove(jid);
	}
	
	public Jid getOptimizationJid() {
		return optimizationJid;
	}
	
	public String getSimulationId() {
		return simulationId;
	}

	public synchronized void addManagerConfigured() {
		managerConfigured++;
		// If all the managers are configured the Simulation Orchestrator configure the Optmization Tool
		if(managerConfigured==this.availableManagers.size()) {
			this.transferFile(optimizationJid.asEntityFullJidIfPossible(), configurationFile, simulationId);
		}
	}
}