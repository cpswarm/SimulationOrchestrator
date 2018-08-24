package simulation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
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
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.gson.Gson;

import it.ismb.pert.cpswarm.mqttlib.transport.MqttAsyncDispatcher;
import messages.progress.GetProgress;
import messages.server.Server;
import messages.start.StartOptimization;
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
	private MqttAsyncDispatcher client;
	private ConnectionListenerImpl connectionListener;
	//private RosterListener rosterListener;
	private String serverName = null;
	private Map<EntityFullJid, Server> simulationManagers = null;
	private String inputDataFolder = null;
	private String outputDataFolder = null;
	private int managerConfigured = 0;
	private List<EntityFullJid> availableManagers = null;
	private String configurationFile = null;
	private Jid optimizationToolJid = null;
	private String optimizationId = null;
	private Boolean monitoring = null;
	private Boolean guiEnabled = false;
	
	public static void main (String args[]) {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder;
		String serverURI = "";
		String serverName = "";
		String serverPassword = "";
		String inputDataFolder = "";
		String outputDataFolder = "";
		String optimizationToolUser = "";
		String optimizationId = "";
		Boolean guiEnabled = false;
		Boolean monitoring = null;
		String mqttBroker = null;
		try {
			Options options = new Options();

			Option input = new Option("s", "src", true, "input folder path");
			input.setRequired(true);
			options.addOption(input);

			Option output = new Option("t", "target", true, "output folder path");
			output.setRequired(true);
			options.addOption(output);

			Option id = new Option("i", "id", true, "optimization ID");
			id.setRequired(true);
			options.addOption(id);
			
			Option gui = new Option("g", "gui", true, "GUI to be used or not for the simulation");
			gui.setType(Boolean.class);
			gui.setRequired(false);
			options.addOption(gui);
			
			CommandLineParser parser = new DefaultParser();
			HelpFormatter formatter = new HelpFormatter();
			CommandLine cmd = null;

			try {
				cmd = parser.parse(options, args);
			} catch (ParseException e) {
				System.out.println(e.getMessage());
				formatter.printHelp("utility-name", options);

				System.exit(1);
			}

			inputDataFolder = cmd.getOptionValue("src");
			outputDataFolder = cmd.getOptionValue("target");
			optimizationId = cmd.getOptionValue("id");
			if(cmd.getOptionValue("gui")!=null) {
				guiEnabled =  Boolean.valueOf(cmd.getOptionValue("gui")); 
			}

			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(SimulationOrchestrator.class.getResourceAsStream("/orchestrator.xml"));
			serverURI = document.getElementsByTagName("serverURI").item(0).getTextContent();
			serverName = document.getElementsByTagName("serverName").item(0).getTextContent();
			serverPassword = document.getElementsByTagName("serverPassword").item(0).getTextContent();
			optimizationToolUser = document.getElementsByTagName("optimizationUser").item(0).getTextContent();
			
			monitoring = Boolean.parseBoolean(document.getElementsByTagName("monitoring").item(0).getTextContent());
			if(monitoring) {
				mqttBroker = document.getElementsByTagName("mqttBroker").item(0).getTextContent();
			}
			if(!inputDataFolder.endsWith("\\") && OsUtils.isWindows()) {
				inputDataFolder+="\\";
			} else if (!inputDataFolder.endsWith("/") && !OsUtils.isWindows()) {
				inputDataFolder+="/";
			}
			if(!new File(inputDataFolder).isDirectory()) {
				System.out.println("Data folder must be a folder");
				return;
			}
		} catch (ParserConfigurationException | SAXException | IOException e1) {
			e1.printStackTrace();
			return;
		} 
		new SimulationOrchestrator(serverURI, serverName, serverPassword, inputDataFolder, outputDataFolder, optimizationToolUser, monitoring, mqttBroker, optimizationId, guiEnabled);
		while(true) {}
	}
	
	/**
	 * 
	 * @param serverIP
	 * 		IP of the XMPP server
	 * @param serverName
	 * 		Name of the XMPP server
	 * @param serverPassword
	 * 		Password to be used to connect to the XMPP server
	 * @param inputDataFolder
	 *      Folder to be used as source for the files
	 * @param outputDataFolder
	 * 		Folder to be used to store the files
	 * @param optimizationToolUser
	 * 		JID of the Optimization Tool
	 * @param monitoring
	 * 		Flag to enable or disable the thread which monitor the progress of the optimization process
	 * @param mqttBroker
	 * 		If the monitor is enabled, this is the IP of the MQTT broker where the messages are forwarded
	 * @param optimizationId
	 * 		ID of the optimization process
	 * @param guiEnabled
	 * 		Indicates if the GUI is enabled or not
	 */
	public SimulationOrchestrator(final String serverIP, final String serverName, final String serverPassword, final String inputDataFolder, final String outputDataFolder, final String optimizationToolUser, final boolean monitoring, final String mqttBroker, final String optimizationId, final Boolean guiEnabled) {
		this.serverName = serverName;
		this.inputDataFolder = inputDataFolder;
		this.outputDataFolder = outputDataFolder;
		this.simulationManagers = new HashMap<EntityFullJid, Server>();
		this.monitoring = monitoring;
		this.optimizationId = optimizationId;
		this.guiEnabled = guiEnabled;
		try {
			this.optimizationToolJid = JidCreate.from(optimizationToolUser+"@"+serverName+"/"+RESOURCE);

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
			
			Thread.sleep(2000);
			final Presence presence = new Presence(Presence.Type.available);
			presence.setStatus("Pronto");
			try {
				connection.sendStanza(presence);
			} catch (final NotConnectedException | InterruptedException e) {
				e.printStackTrace();
			}
			
			// If the monitoring is needed, it instantiates also the MQTT broker
			if(monitoring) {
				client = new MqttAsyncDispatcher(mqttBroker, UUID.randomUUID().toString(), null,
						null, true, null);
				// connect the client
				client.connect();
				while(!client.isConnected()){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
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
    	Zipper zipper = new Zipper(inputDataFolder);
		configurationFile = zipper.generateFileList(new File(inputDataFolder));
    	String[] fileNameParts = (inputDataFolder+"test.zip").split("\\.");
    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss");
		Date date = new Date();
		String fileName = fileNameParts[0] + "_" + dateFormat.format(date) + "." + fileNameParts[1];
    	zipper.zipIt(fileName);
    	availableManagers = new ArrayList<EntityFullJid>();
    	for(EntityFullJid account : simulationManagers.keySet()) {
    		if(simulationManagers.get(account).compareTo(serverCompare)>0) {
    			if(!availableManagers.contains(account))
    				availableManagers.add(account); 
    		}
    	}
    	for (EntityFullJid availableManager : availableManagers) {
    		System.out.println("Configuring the simulation manager: "+availableManager);
    		this.transferFile(availableManager, fileName, optimizationId);
    	}
    	//It deletes the zip file
    	File file = new File(fileName);
		if(file.delete()){
			System.out.println(file.getName() + " is deleted!");
		}else{
			System.out.println("Delete operation is failed.");
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
	 * This method verifies if the receiver supports the file transfer and in
	 * this case it sends a file
	 */
	public void transferFile(final EntityFullJid receiver, final String filePath, final String message) {
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
				if (!group.contains(optimizationToolJid.asBareJid())) {
					roster.createEntry(optimizationToolJid.asBareJid(),
							"optimization", groups);
				} 
			} else {
				roster.createEntry(optimizationToolJid.asBareJid(),
						"optimization", groups);
			}			
			
		} catch (NotLoggedInException | NoResponseException | XMPPErrorException
				| NotConnectedException | InterruptedException e) {
			// The client is disconnected
			System.out.println(
					"Connection disconnected, adding system bundles to roster interrupted");
			e.printStackTrace();
		} 
	}
	

	public synchronized void addManagerConfigured() {
		managerConfigured++;
		// If all the managers are configured the Simulation Orchestrator configure the Optmization Tool
		if(managerConfigured==this.availableManagers.size()) {
			sendStartOptimization("");
		}
	}
	
	public boolean sendGetProgress() {
		Gson gson = new Gson();
		GetProgress getProgress = new GetProgress();
		getProgress.setID(this.optimizationId);
		ChatManager manager = ChatManager.getInstanceFor(connection);
		Chat chat = manager.chatWith(this.optimizationToolJid.asEntityBareJidIfPossible());
		Message message = new Message();
		message.setBody(gson.toJson(getProgress));
		try {
			chat.send(message);
		} catch (NotConnectedException | InterruptedException e) {
			System.out.println("Error sending GetProgress message");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	
	private boolean sendStartOptimization(final String params) {
		Gson gson = new Gson();
		StartOptimization start = new StartOptimization();
		start.setThreads(availableManagers.size());
		start.setID(this.optimizationId);
		start.setGui(guiEnabled);
		start.setParams(params);
		List<String> managersJid = new ArrayList<String>();
		for(EntityFullJid availableManager : this.availableManagers) {
			managersJid.add(availableManager.toString());
		}
		start.setSimulationManagers(managersJid);
		System.out.println("Sending StartOptimization message: "+gson.toJson(start));
		ChatManager manager = ChatManager.getInstanceFor(connection);
		Chat chat = manager.chatWith(this.optimizationToolJid.asEntityBareJidIfPossible());
		Message message = new Message();
		message.setBody(gson.toJson(start));
		try {
			chat.send(message);
		} catch (NotConnectedException | InterruptedException e) {
			System.out.println("Error sending StartOptimization message");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public String getConfigurationFile() {
		return configurationFile;
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
		return optimizationToolJid;
	}
	
	public String getSimulationId() {
		return optimizationId;
	}
	
	public Boolean isMonitoring( ) {
		return monitoring;
	}
	
	public MqttAsyncDispatcher getMqttClient() {
		return client;
	}
}