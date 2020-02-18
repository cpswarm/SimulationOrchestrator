package simulation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import javax.net.ssl.SSLContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.ReconnectionManager.ReconnectionPolicy;
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
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import config.deployment.Deployment;
import config.deployment.DeploymentConfiguration;
import config.deployment.Service;
import config.frevo.FrevoConfiguration;
import config.modelio.Parameter;
import config.modelio.Parameters;
import eu.cpswarm.optimization.messages.GetProgressMessage;
import eu.cpswarm.optimization.messages.MessageSerializer;
import eu.cpswarm.optimization.messages.ParameterSet;
import eu.cpswarm.optimization.messages.RunSimulationMessage;
import eu.cpswarm.optimization.messages.StartOptimizationMessage;
import it.links.pert.codegen.generator.CodeGenerator;
import it.links.pert.codegen.generator.CodeGeneratorFactory;
import it.links.pert.codegen.generator.CodeGeneratorType;
import it.links.pert.codegen.scxml.SCXML2RosGenerator;
import messages.server.Capabilities;
import messages.server.Server;
import simulation.kubernetes.KubernetesUtils;
import simulation.tools.ChoiceOption;
import simulation.tools.Zipper;
import simulation.xmpp.ConnectionListenerImpl;
import simulation.xmpp.MessageEventCoordinatorImpl;
import simulation.xmpp.PacketListenerImpl;
import simulation.xmpp.SubscriptionHandler;


public class SimulationOrchestrator {
	private static final String RESOURCE = "cpswarm";
	public static final Semaphore SEMAPHORE = new Semaphore(1);
	private BlockingQueue <Presence> queue;
	private XMPPTCPConnection connection;
	private ConnectionListenerImpl connectionListener;
	//private RosterListener rosterListener;
	private String serverName = null;
	private Map<EntityBareJid, Server> simulationManagers = null;
	private String inputDataFolder = null;
	private String outputDataFolder = null;
	private int managerConfigured = 0;
	private List<EntityBareJid> availableManagers = null;
	private Jid optimizationToolJid = null;
	private String optimizationId = null;
	// JSon containing the Optimization Configuration 
	// TODO receive these configurations from the Launcher
	private FrevoConfiguration optimizationConfiguration = null;
	private String simulationConfiguration = null;
	private Server server;
	private String taskId;
	private String serverUsername = "";
	private String serverPassword = "";
	private Boolean optimizationEnabled = null;
	private String configurationFolder = null;
	private boolean localSimulationManager = false;
	private static boolean TEST = true;
	private Boolean simulationDone = null;
	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static final int MAX_N_OF_ATTEMPTS = 10;
	private Boolean configEnabled = null;
	private int startingTimeout;
	private String scxmlPath;
	private String adfPath;
	private String simulationEnv;
	
	public static enum OP_MODE {G, D,  S, DS;
		
		static OP_MODE fromString(String text) {
			switch(text) {
			case "G":
				return OP_MODE.G;
			case "D":
				return OP_MODE.D;
			case "S":
				return OP_MODE.S;
			case "DS":
				return OP_MODE.DS;
			default:
				return null;
			}
		}
	
	}
	
	private OP_MODE opMode;
	
	public static void main (String args[]) {
		TEST = false;
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder;
		InetAddress serverURI = null;
		String serverName = "";
		String serverUsername = "";
		String serverPassword = "";
		String inputDataFolder = "";
		String outputDataFolder = "";
		String optimizationToolUser = "";
		String taskId = "";
		String parameters = "";
		String dimensions = "";
		String scxml = "";
		String adfFile = "";
		String envSim = "";
		Long maxAgents = null;
		Boolean guiEnabled = false;
		Boolean optimizationEnabled = false;
		String configurationFolder = null;
		String optimizationToolPath = null;
		Boolean localOptimization = false;
		String optimizationToolPassword = "";
		Boolean localSimulationManager = false;
		String simulationManagerPath = null;
		FrevoConfiguration optConf = null;
		Boolean configEnabled = false;
		int startingTimeout = 5000;
		OP_MODE opMode = null;
			
		try {
			Options options = new Options();

			ChoiceOption mode = new ChoiceOption("M", "mode", true, "Running mode for the SOO", "g", "d", "s","ds");
			mode.setRequired(true);
			options.addOption(mode);
			
			Option input = new Option("s", "src", true, "input folder path");
			input.setRequired(false);
			options.addOption(input);

			Option output = new Option("t", "target", true, "output folder path");
			output.setRequired(false);
			options.addOption(output);
			
			Option configuration = new Option("c", "conf", true, "folder with the configuration files");
			configuration.setRequired(false);
			options.addOption(configuration);

			Option id = new Option("i", "id", true, "Task ID");
			id.setRequired(false);
			options.addOption(id);
			
			Option gui = new Option("g", "gui", false, "GUI to be used or not for the simulation");
			gui.setRequired(false);
			options.addOption(gui);
			
			Option params = new Option("p", "params", true, "Parameters to be passed to the simulator");
			params.setRequired(false);
			options.addOption(params);
			
			Option dim = new Option("d", "dim", true, "Number of dimensions required for simulation");
			dim.setRequired(false);
			options.addOption(dim);
			
			Option max = new Option("m", "max", true, "Maximum number of agents required for simulation");
			max.setRequired(false);
			options.addOption(max);
			
			Option optimization = new Option("o", "opt", false, "Indicates if the optimization is required or not");
			optimization.setRequired(false);
			options.addOption(optimization);
			
			Option candidateCount = new Option("cc", "can", true, "Indicates the candidate count");
			optimization.setRequired(false);
			options.addOption(candidateCount);

			Option generationCount = new Option("gc", "gen", true, "Indicates the generation count");
			optimization.setRequired(false);
			options.addOption(generationCount);
			
			Option simulationTimeout = new Option("st", "sim", true, "Indicates the the simulation timeout for the OT");
			optimization.setRequired(false);
			options.addOption(simulationTimeout);

			Option seed = new Option("se", "seed", true, "Indicates the seed to be used in the OT");
			optimization.setRequired(false);
			options.addOption(seed);

			Option scxmlOpt = new Option("sc", "scxml", true, "State Machine file path");
			optimization.setRequired(false);
			options.addOption(scxmlOpt);
			
			Option adf = new Option("a", "adf", true, "Abstract Description file path");
			optimization.setRequired(false);
			options.addOption(adf);			

			Option env = new Option("es", "env", true, "Environment for Simulation");
			optimization.setRequired(false);
			options.addOption(env);			
			
			CommandLineParser parser = new DefaultParser();
			HelpFormatter formatter = new HelpFormatter();
			CommandLine cmd = null;

			try {
				cmd = parser.parse(options, args);
			} catch (ParseException e) {
				System.out.println(e.getMessage());
				formatter.printHelp("java -jar soo.jar", options);

				System.exit(1);
			}
			
			opMode = OP_MODE.fromString(cmd.getOptionValue("mode").toUpperCase());
				
			configurationFolder = cmd.getOptionValue("conf");
			
			if(!(opMode.equals(OP_MODE.D) || opMode.equals(OP_MODE.G))) {
				inputDataFolder = cmd.getOptionValue("src");
				outputDataFolder = cmd.getOptionValue("target");
				taskId = cmd.getOptionValue("id");
				if(taskId==null || taskId.isEmpty()) {
					System.out.println("The task ID cannot be empty");
					return;
				}
				dimensions = cmd.getOptionValue("dim");

				maxAgents = Long.parseLong(cmd.getOptionValue("max"));

				optimizationEnabled = cmd.hasOption("opt");

				guiEnabled = cmd.hasOption("gui");
				
				if(cmd.getOptionValue("params")!=null) {
					parameters = cmd.getOptionValue("params");
				}

				String can = "100";
				if(cmd.getOptionValue("can")!=null) {
					can = cmd.getOptionValue("can");
				}
				String gen = "100";
				if(cmd.getOptionValue("gen")!=null) {
					gen = cmd.getOptionValue("gen");
				}
				String sim = "1200";
				if(cmd.getOptionValue("sim")!=null) {
					sim = cmd.getOptionValue("sim");
				}
				String se = "0";
				if(cmd.getOptionValue("seed")!=null) {
					se = cmd.getOptionValue("seed");
				}		
				
				Gson gson = new Gson();
				JsonReader reader = new JsonReader(new InputStreamReader(SimulationOrchestrator.class.getResourceAsStream("/frevoConfiguration.json")));
				optConf = gson.fromJson(reader, FrevoConfiguration.class);
				optConf.setCandidateCount(Integer.parseInt(can));
				optConf.setGenerationCount(Integer.parseInt(gen));
				optConf.setSimulationTimeoutSeconds(Integer.parseInt(sim));
				optConf.setEvaluationSeed(Integer.parseInt(se));
				
			}	
			if(opMode.equals(OP_MODE.G)) {
				if(cmd.getOptionValue("scxml")!=null) {
					scxml = cmd.getOptionValue("scxml");
				}
				if(cmd.getOptionValue("adf")!=null) {
					adfFile = cmd.getOptionValue("adf");
				}
				if(cmd.getOptionValue("env")!=null) {
					envSim = cmd.getOptionValue("env");
				}				
			}
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(SimulationOrchestrator.class.getResourceAsStream("/orchestrator.xml"));
			serverURI = InetAddress.getByName(document.getElementsByTagName("serverURI").item(0).getTextContent());
			serverName = document.getElementsByTagName("serverName").item(0).getTextContent();
			serverUsername = document.getElementsByTagName("username").item(0).getTextContent();
			configEnabled = Boolean.parseBoolean(document.getElementsByTagName("configEnabled").item(0).getTextContent());
			startingTimeout = Integer.parseInt(document.getElementsByTagName("startingTimeout").item(0).getTextContent());
			serverPassword = document.getElementsByTagName("serverPassword").item(0).getTextContent();
			optimizationToolUser = document.getElementsByTagName("optimizationUser").item(0).getTextContent();
			localOptimization = Boolean.parseBoolean(document.getElementsByTagName("localOptimization").item(0).getTextContent());
			optimizationToolPassword = document.getElementsByTagName("optimizationToolPassword").item(0).getTextContent();
			if(localOptimization) {
				optimizationToolPath = document.getElementsByTagName("optimizationToolPath").item(0).getTextContent();
			}
			localSimulationManager = Boolean.parseBoolean(document.getElementsByTagName("localSimulationManager").item(0).getTextContent());
			if(localSimulationManager) {
				simulationManagerPath = document.getElementsByTagName("simulationManagerPath").item(0).getTextContent();
			}
			if(!inputDataFolder.endsWith(File.separator)) {
				inputDataFolder+=File.separator;
			} 
			if(!outputDataFolder.endsWith(File.separator)) {
				outputDataFolder+=File.separator;
			}
			// Only in generation mode the configuration is not required
			if(!opMode.equals(OP_MODE.G)) {
				if(!configurationFolder.endsWith(File.separator)) {
					configurationFolder+=File.separator;
				}
				if(opMode.equals(OP_MODE.S) && optimizationEnabled) {
					Gson gson = new Gson();
					JsonReader reader = new JsonReader(new FileReader(configurationFolder+"parameters.json"));
					Parameters modelledParams =  gson.fromJson(reader, Parameters.class);
					List<config.frevo.Parameter> frevoParameters = new ArrayList<config.frevo.Parameter>();
					for (Parameter param : modelledParams.getParameters()) {
						config.frevo.Parameter frevoParaneter = new config.frevo.Parameter();
						frevoParaneter.setName(param.getName());
						frevoParaneter.setMetaInformation(param.getMeta());
						frevoParaneter.setMinimum(param.getMin().intValue());
						frevoParaneter.setMaximum(param.getMax().intValue());
						frevoParaneter.setScale(Double.parseDouble(param.getScale()));
						frevoParameters.add(frevoParaneter);
					}
					optConf.getRepresentationBuilder().setParameters(frevoParameters);
				}
			} 
			if(!new File(inputDataFolder).isDirectory()) {
				System.out.println("src must be a folder");
				return;
			}
		} catch (ParserConfigurationException | SAXException | IOException e1) {
			e1.printStackTrace();
			return;
		} 
		new SimulationOrchestrator(opMode, serverURI, serverName, serverUsername, serverPassword, inputDataFolder, outputDataFolder, optimizationToolUser, taskId, guiEnabled, parameters, dimensions, maxAgents, optimizationEnabled, configurationFolder, localOptimization, optimizationToolPath, optimizationToolPassword, localSimulationManager, simulationManagerPath, optConf, configEnabled, startingTimeout, scxml, adfFile, envSim);
		while(true) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param mode
	 *      Operation mode of the SOO
	 * @param serverIP
	 * 		IP of the XMPP server
	 * @param serverName
	 * 		Name of the XMPP server
	 * @param serverUsername
	 *      Username to be used to connect to the XMPP server
	 * @param serverPassword
	 * 		Password to be used to connect to the XMPP server
	 * @param inputDataFolder
	 *      Folder to be used as source for the files
	 * @param outputDataFolder
	 * 		Folder to be used to store the files
	 * @param optimizationToolUser
	 * 		JID of the Optimization Tool
	 * @param taskId
	 * 		ID of the task
	 * @param guiEnabled
	 * 		Indicates if the GUI is enabled or not
	 * @param parameters
	 * 		Parameters to be used for the simulation
	 * @param dimensions
	 * 		Number of dimensions required for the simulation
	 * @param maxAgents
	 * 		Maximum number of agents required for the simulation
	 * @param optimization
	 *      Indicates if the optimization is enabled or not
	 * @param configurationFolder
	 *      Folder with the configuration files
	 * @param localOptimization
	 * 		Indicates if the Optimization Tool has to be launched by the Orchestrator
	 * @param optimizationToolPath
	 * 		Path of the Optimization Tool
	 * @param localSimulationManager
	 * 		Indicates if the Simulation Manager has to be launched by the Orchestrator
	 * @param simulationManagerPath
	 * 		Path of the Simulation Manager
	 * @param optimizationToolPassword
	 *     To be used to start the optimization tool directly from the orchestrator (localOptimization = true)
	 * @param optimizationConfiguration
	 * 	 	Configuration parameters to be sent to the optimization Tool
	 * @param configEnabled
	 *       Indicates if the configuration of the simulators must be done or not
	 * @param startingTimeout
	 * 		Time to wait for the subscription of new Simulation Managers
	 * @param scxml
	 * 		Path to the state machine file to be used to generate the package for simulation
	 * @parm adfFile
	 * 		Path to the Abstrat Description file to be used to generate the package for simulation
	 * @param envSim
	 *      Environment to be used for simulation
	 * 
	 */
	public SimulationOrchestrator(final OP_MODE opMode, final InetAddress serverIP, final String serverName, final String serverUsername, final String serverPassword, final String inputDataFolder, final String outputDataFolder, final String optimizationToolUser, final String taskId, final Boolean guiEnabled, final String parameters, final String dimensions, final Long maxAgents, final Boolean optimization, final String configurationFolder, final Boolean localOptimization,  final String optimizationToolPath, final String optimizationToolPassword, final Boolean localSimulationManager,  final String simulationManagerPath, final FrevoConfiguration optConf, final Boolean configEnabled, int startingTimeout, final String scxml, final String adf, final String env) {
		this.opMode = opMode;
		this.taskId = taskId;
		this.serverName = serverName;
		this.inputDataFolder = inputDataFolder;
		this.outputDataFolder = outputDataFolder;
		this.serverUsername = serverUsername;
		this.serverPassword = serverPassword;
		this.simulationManagers = new HashMap<EntityBareJid, Server>();
		this.optimizationId = taskId+"!"+UUID.randomUUID();
		this.simulationConfiguration = "gui:=" + (guiEnabled? "true":"false") + parameters.toString();
		this.optimizationEnabled = optimization;
		this.configurationFolder = configurationFolder;
		this.localSimulationManager = localSimulationManager;
		this.optimizationConfiguration = optConf;
		this.configEnabled = configEnabled;
		this.startingTimeout = startingTimeout;
		this.scxmlPath = scxml;
		this.adfPath = adf;
		this.simulationEnv = env;
		// If the SOO is run in Generation mode, it doesn't need to connect to the server, 
		// since it doesn't need the distributed simulation managers
		if(opMode.equals(opMode.G)) {
			this.generateCode();
			return;
		}
		
		server = new Server();
		server.setServer("Orchestrator");
		Capabilities caps = new Capabilities();
		caps.setDimensions(dimensions);
		caps.setMaxAgents(maxAgents);
		server.setCapabilities(caps);
		try {
			if(this.optimizationEnabled && localOptimization) {
				String optimizationToolParameters = "-n "+ this.serverName + " -ip " + serverIP + " -p 5222 -r "+RESOURCE +" -cid "+optimizationToolUser+ " -cp "+optimizationToolPassword + " -c "+this.outputDataFolder+"candidate";
				OptimizationToolLauncher launcher = new OptimizationToolLauncher(optimizationToolPath, optimizationToolParameters);
				Thread thread = new Thread(launcher);
				thread.start();
			}
			this.optimizationToolJid = JidCreate.from(optimizationToolUser+"@"+serverName+"/"+RESOURCE);

			if(this.localSimulationManager) {
				SimulationManagerLauncher launcher = new SimulationManagerLauncher(simulationManagerPath, simulationConfiguration);
				Thread thread = new Thread(launcher);
				thread.start();				
			}
			
			final SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, null, new SecureRandom());

			XMPPTCPConnectionConfiguration connectionConfig = XMPPTCPConnectionConfiguration
					.builder().setHostAddress(serverIP).setPort(5222)
					.setXmppDomain(serverName)
					.setCompressionEnabled(false).setCustomSSLContext(sc).build();
			connection = new XMPPTCPConnection(connectionConfig);
			
			final StanzaFilter presenceFilter = new StanzaTypeFilter(
					Presence.class);
			System.out.println("adding the packet listener to the local connection");
			// This listener checks the presences
			final PacketListenerImpl packetListener = new PacketListenerImpl(
					this);
			this.addAsyncStanzaListener(packetListener, presenceFilter);
			
			connection.connect();

			connection.login(serverUsername, serverPassword , Resourcepart.from(RESOURCE));
			System.out.println("Connected to server");

			connectionListener = new ConnectionListenerImpl(this);
			// Adds a listener for the status of the connection
			connection.addConnectionListener(connectionListener);

			ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
			reconnectionManager.enableAutomaticReconnection();
			reconnectionManager.setReconnectionPolicy(ReconnectionPolicy.RANDOM_INCREASING_DELAY);
			
			// Adds the listener for the incoming messages
			ChatManager.getInstanceFor(connection).addIncomingListener(new MessageEventCoordinatorImpl(this));
			
			do {
				Thread.sleep(1000);
			}while(!connection.isConnected() || !connection.isAuthenticated());
			
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
			System.out.println("msg "+e.getMessage());
			System.out.println("loc "+e.getLocalizedMessage());
			System.out.println("cause "+e.getCause());
			System.out.println("excep "+e);
			e.printStackTrace();
			return;
		} catch(Exception me) {
			System.out.println("msg "+me.getMessage());
			System.out.println("loc "+me.getLocalizedMessage());
			System.out.println("cause "+me.getCause());
			System.out.println("excep "+me);
			me.printStackTrace();
			return;
		}

		SubscriptionHandler handler = new SubscriptionHandler(this);
		Thread thread = new Thread(handler);
		thread.start();

		addOptimizationToTheRoster();
		
		switch(opMode) {
		case D:
			this.deploySimulators();
			break;
		case DS:
			this.deploySimulators();
		case S:
			// In case of test the evaluation is done only after that the dummy manager is started
			if(!TEST) {
				this.evaluateSimulationManagers();
			}
		}
	}
    
    private boolean createAccount(final String password) {
		final AccountManager accountManager = AccountManager
				.getInstance(connection);
		final HashMap<String, String> props = new HashMap<String, String>();
		// The description will be the property name of the account
		props.put("name", "server");
		Localpart part;
		try {
			part = Localpart.from(serverUsername);
			connection.connect();
			accountManager.createAccount(part, password, props);
			connection.login(serverUsername, password, Resourcepart.from(RESOURCE));
		} catch (InterruptedException | SmackException | IOException | XMPPException me) {
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
			return false;
		}
		addOptimizationToTheRoster();
		this.evaluateSimulationManagers();
		return true;
    }
    
    public void evaluateSimulationManagers() {
		try {
			System.out.println("Wait to collect the managers");
			Thread.sleep(this.startingTimeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	this.evaluateSimulationManagers(server);
    }
    
    
    public void evaluateSimulationManagers(Server serverCompare) {
    	this.managerConfigured=0;
    	System.out.println("Evalauting available managers: "+ Arrays.toString(simulationManagers.keySet().toArray()));
    	String fileName = null;
    	if((TEST && (inputDataFolder == null || configurationFolder==null)) || !configEnabled) {
    		File file = new File("src/main/resources/file.xsd");
    		fileName = file.getAbsolutePath();
    	} else {
    		Zipper zipper = new Zipper(inputDataFolder);
    		zipper.generateFileList(new File(inputDataFolder));
    		zipper.updateSourceFolder(configurationFolder);
    		zipper.generateFileList(new File(configurationFolder));
    		String[] fileNameParts = (inputDataFolder+"test.zip").split("\\.");
    		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss");
    		Date date = new Date();
    		fileName = fileNameParts[0] + "_" + dateFormat.format(date) + "." + fileNameParts[1];
    		zipper.zipIt(fileName);
    	}
    	availableManagers = new ArrayList<EntityBareJid>();
    	for(EntityBareJid account : simulationManagers.keySet()) {
    		if(simulationManagers.get(account)!=null && simulationManagers.get(account).compareTo(serverCompare)>0) {
    			if(!availableManagers.contains(account)) {
    				availableManagers.add(account);
    				// If there is not optimization the first simulator available is selected
    				if(!optimizationEnabled) {
    					break;
    				}
    			}
    		}
    	}
    	for (EntityBareJid availableManager : availableManagers) {
    		if(!TEST && configEnabled) {
    			System.out.println("Configuring the simulation manager: "+availableManager);
    			try {
    				this.transferFile(JidCreate.entityFullFrom(availableManager.toString()+"/"+RESOURCE), fileName, taskId);
    			} catch (XmppStringprepException e) {
    				e.printStackTrace();
    			}
    		} else {
    			this.addManagerConfigured();
    		}
    	}
    	if(!TEST || configEnabled) {
    		//It deletes the zip file
    		File file = new File(fileName);
    		if(file.delete()){
    			System.out.println(file.getName() + " is deleted!");
    		}else{
    			System.out.println("Delete operation is failed.");
    		}
    	}
    }

    
    private void generateCode() {
    	//Create map for Code Generator options
    	Map<String, String> options = new HashMap<String, String>();
    	options.put("scxmlPath", this.scxmlPath);
		options.put("adfPath", this.adfPath);
		options.put("mode", "simulation");
    	CodeGenerator cg = null;
    	switch(this.simulationEnv) {
    	case "ROS":
    		cg = CodeGeneratorFactory.getInstance(CodeGeneratorType.SCXML2ROS, this.outputDataFolder, options);
    		break;
    	default:
    		cg = CodeGeneratorFactory.getInstance(CodeGeneratorType.SCXML2ROS, this.outputDataFolder, options);
    	}
    	if(cg.generate()) {
    		System.out.print("Code Generated in: "+this.outputDataFolder);
    	} else {
    		System.out.println("Error generating simulation package");
    	}
    	return;
    }
    
    private void deploySimulators() {
		Gson gson = new Gson();
		JsonReader reader;
		DeploymentConfiguration deployConf = null;
		try {
			reader = new JsonReader(new FileReader(this.configurationFolder+"deployment.json"));
			deployConf = gson.fromJson(reader, DeploymentConfiguration.class);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (Deployment deployment: deployConf.getDeployments()) {
			KubernetesUtils.deploy(deployment);
		}
		if(deployConf.getServices()!=null) {
			for (Service service : deployConf.getServices()) {
				KubernetesUtils.installService(service);
			}
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
				System.out.println("File sent, waiting transfer complete");
				while (!transfer.isDone() && transfer.getException()==null) {
					Exception ex = transfer.getException();
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
				System.out.println("Error in file transfer" + transfer.getError());
			} else if (status == Status.complete) {
				System.out.println("File transfered");
			}
		} else {
			System.out.println("Error, the Simulation manager: "+receiver+" doesn't support the file transfer");
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
			if(optimizationEnabled) {
				sendStartOptimization();
			} else {
				sendRunSimulation();
			}
		}
	}
	
	public boolean sendGetProgress() {
		if(!connection.isConnected()) {
			//the connection need to be reconnected
			this.reconnect();
			do {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while(connection.isConnected());
		}
		GetProgressMessage getProgress = new GetProgressMessage(optimizationId, "Get Progress message");
		ChatManager manager = ChatManager.getInstanceFor(connection);
		Chat chat = manager.chatWith(this.optimizationToolJid.asEntityBareJidIfPossible());
		Message message = new Message();
		MessageSerializer serializer = new MessageSerializer();
		String messageToSend = serializer.toJson(getProgress);
		message.setBody(messageToSend);
		System.out.println("Sending getProgress "+messageToSend);
		try {
			chat.send(message);
		} catch (NotConnectedException | InterruptedException e) {
			System.out.println("Error sending GetProgress message");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	
	private boolean sendStartOptimization() {
		List<String> managersJid = new ArrayList<String>();
		for(EntityBareJid availableManager : this.availableManagers) {
			managersJid.add(availableManager.toString());
		}
		optimizationConfiguration.getExecutorBuilder().setThreadCount(managersJid.size());
		Gson gson = new Gson();
		StartOptimizationMessage start = new StartOptimizationMessage(this.optimizationId, "Start Optimization message", gson.toJson(optimizationConfiguration), simulationConfiguration, managersJid);
		MessageSerializer serializer = new MessageSerializer();
		String messageToSend = serializer.toJson(start);
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println("Sending StartOptimization message: "+messageToSend+" at "+sdf.format(timestamp));
		ChatManager manager = ChatManager.getInstanceFor(connection);
		Chat chat = manager.chatWith(this.optimizationToolJid.asEntityBareJidIfPossible());
		Message message = new Message();
		message.setBody(messageToSend);
		try {
			chat.send(message);
		} catch (NotConnectedException | InterruptedException e) {
			System.out.println("Error sending StartOptimization message");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	private String readFile(String path, Charset encoding) 
			throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
	private boolean sendRunSimulation() {
		String candidateToSend = "";
		try {
			if(TEST && this.configurationFolder==null) {
				candidateToSend = this.readFile(new File("src/main/resources/candidate.json").getAbsolutePath(), StandardCharsets.UTF_8);
			} else {
				candidateToSend = this.readFile(this.configurationFolder+File.separator+"candidate.json", StandardCharsets.UTF_8);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		Gson gson = new Gson();
		ParameterSet parameters = gson.fromJson(candidateToSend, ParameterSet.class); 
		RunSimulationMessage run = new RunSimulationMessage(this.optimizationId, "Run simulation message", "1", simulationConfiguration, parameters);
		MessageSerializer serializer = new MessageSerializer();
		String messageToSend = serializer.toJson(run);
		System.out.println("Sending RunSimulation message: "+messageToSend);
		ChatManager manager = ChatManager.getInstanceFor(connection);
		Chat chat = manager.chatWith(availableManagers.get(0));
		Message message = new Message();
		message.setBody(messageToSend);
		try {
			chat.send(message);
		} catch (NotConnectedException | InterruptedException e) {
			System.out.println("Error sending RunSimulation message");
			e.printStackTrace();
			return false;
		}
		return true;
	}


	public XMPPTCPConnection getConnection() {
		return connection;
	}
	
	public synchronized void putSimulationManager(EntityBareJid jid, Server server) {
		System.out.println("Adding "+ jid.toString() +" to "+ Arrays.toString(simulationManagers.keySet().toArray()));
		simulationManagers.put(jid,server);
		System.out.println("Available managers: "+ Arrays.toString(simulationManagers.keySet().toArray()));
	}	

	public void removeSimulationManager(Jid jid) {
		simulationManagers.remove(jid);
	}
	
	public Jid getOptimizationJid() {
		return optimizationToolJid;
	}
	
	public String getOptimizationId() {
		return optimizationId;
	}
		
	public Boolean getOptimizationEnabled() {
		return optimizationEnabled;
	}

	public void setOptimizationEnabled(Boolean optimizationEnabled) {
		this.optimizationEnabled = optimizationEnabled;
	}

	public Boolean isSimulationDone() {
		return simulationDone;
	}

	public String getOutputDataFolder() {
		return outputDataFolder;
	}

	public void setOutputDataFolder(String outputDataFolder) {
		this.outputDataFolder = outputDataFolder;
	}

	public void setSimulationDone(boolean simulationDone) {
		System.out.println("Set simulation done");
		this.simulationDone = simulationDone;
	}
	
	public void reconnect() {
		try {
			connection.disconnect();
			Thread.sleep(1000);
			connection.connect();

			connection.login(serverUsername, serverPassword , Resourcepart.from(RESOURCE));
			System.out.println("Connected to server");
		
		} catch (SmackException | IOException | XMPPException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Add a subscribe request to the queue of the ones avialable
	 */
	public void putSubscribeRequest(Presence presence) {
		if (queue==null) {
			queue = new LinkedBlockingQueue<Presence>();
		}
		try {
			queue.put(presence);
		} catch (InterruptedException e) {
			
		}
	}
	
	/**
	 * Take a subscription request from the queue
	 * 
	 * @return
	 */
	public Presence getSubscriptionRequest () {
		if (queue==null) {
			queue = new LinkedBlockingQueue<Presence>();
		}
		try {
			return queue.take();
		} catch (InterruptedException e) {
			return null;
		}
	}
}