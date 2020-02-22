package simulation;

import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.jxmpp.jid.impl.JidCreate;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import eu.cpswarm.optimization.parameters.ParameterOptimizationConfiguration;
import eu.cpswarm.optimization.statuses.SimulationManagerStatus;
import eu.cpswarm.optimization.statuses.StatusSerializer;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.api.model.apps.ReplicaSetList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * Unit test for the Simulation and Optimization Orchestrator and its interaction with the other components of the Simulation and Optimization Environment.
 * The tests for the generation of the simulation package are contained in the other test suite 
 */
@TestMethodOrder(OrderAnnotation.class)
public class AppTest {
	/**
	 * Test running example
	 * 
	 * mvn test -B
	 * -Dtest_server_ip=130.192.86.237 (IP of the XMPP server) 
	 * -Dtest_server_name=pert-demoenergy-virtus.ismb.polito.it (name of the XMPP server) 
	 * -Dtest_server_username=orchestrator (Username to be used by the orchestrator to authenticate in the XMPP server)
	 * -Dtest_server_password=orchestrator (Password to be used by the orchestrator to authenticate in the XMPP server)
	 * -Dtest_orchestrator_input_data_folder=/home/cpswarm/Desktop/cpswarm/ (folder containing the input files) -- Optional
	 * -Dtest_orchestrator_output_data_folder=/home/cpswarm/Desktop/cpswarm-out (folder where the output files will be inserted) -- Optional
	 * -Dtest_manager_data_folder=/home/cpswarm/Desktop/output/ (data folder used by the simulation manager) -- Optional
	 * -Doptimization_user=optimization_test (XMPP username used for the Optimization Tool)
	 * -Dot_data_folder=/home/cpswarm/Desktop/ot/  (folder used by the Optimization Tool) -- Optional
	 * -Dros_folder=/home/cpswarm/Desktop/test/src/emergency_exit/src/ (Folder used for the ROS package to start the first simulation) -- Optional
	 * -Drecovery=true (Flag to enable or disable the thread which monitor the progress of the optimization process)
	 * -Dgui_enabled=false (indicates if the GUI has to be used during the simulations)
	 * -Dparameters="" (indicates the parameters to be used in the simulations)
	 * -Ddimensions = "2D" (indicates the number of dimensions required for the simulation)
	 * -Dmax_agents = "8" (indicates the maximum number of agents required for the simulation)
	 * -Dconfiguration_folder=/home/cpswarm/Desktop/configuration/ (path with the configuration files) -- Optional
	 * -Dlocal_optimization=true (Indicates if the Simulation Orchestator has to launch also the Optimization Tool)
	 * -Doptimization_tool_path=/home/cpswarm/Desktop/ (path of the executable of the Optimization Tool)
	 * -Doptimization_tool_password = blah  (To be used to launch the optimization tool from the orchestrator)
	 * -Dlocal_simulation_manager=true (Indicates if the Simulation Orchestator has to launch also the Simulation Manager)
	 * -Dsimulation_manager_path=/home/cpswarm/Desktop/ (path of the executable of the Simulation Manager) 
	 * -Dstarting_timeout=5000 (time to wait for the subscription of new Simulation Managers)
	 * -Djavax.xml.accessExternalDTD=all (configuration for xml parsing)
	 * 
	 */
	private String serverName = System.getProperty("test_server_name");
	private String serverUsername = System.getProperty("test_server_username");
	private String serverPassword = System.getProperty("test_server_password");
	private String orchestratorInputDataFolder = System.getProperty("test_orchestrator_input_data_folder");
	private String orchestratorOutputDataFolder = System.getProperty("test_orchestrator_output_data_folder");
	private String managerDataFolder = System.getProperty("test_manager_data_folder");
	private String optimizationUser = System.getProperty("optimization_user");
	private String otDataFolder = System.getProperty("ot_data_folder");
	private String rosFolder = System.getProperty("ros_folder");
	private Boolean recovery = Boolean.parseBoolean(System.getProperty("recovery"));
	private Boolean guiEnabled = Boolean.parseBoolean(System.getProperty("gui_enabled"));
	private String parameters = System.getProperty("parameters");
	private String dimensions = System.getProperty("dimensions");
	private int maxAgents = Integer.parseInt(System.getProperty("max_agents"));
	private String configurationFolder = System.getProperty("conf_folder");
	private Boolean localOptimization = Boolean.parseBoolean(System.getProperty("local_optimization"));
	private String optimizationToolPath = System.getProperty("optimzation_tool_path");
	private String optimizationToolPassword = System.getProperty("optimization_tool_password");
	private Boolean localSimulationManager = Boolean.parseBoolean(System.getProperty("local_simulation_manager"));
	private String simulationManagerPath = System.getProperty("simulation_manager_path");
	private static ParameterOptimizationConfiguration optimizationConfiguration = null;
	private int startingTimeout = Integer.parseInt(System.getProperty("starting_timeout"));
	private static InetAddress serverIPAddress = null;
	
	@BeforeAll
	static void setUp() {
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new InputStreamReader(SimulationOrchestrator.class.getResourceAsStream("/frevoConfiguration.json")));
		optimizationConfiguration = gson.fromJson(reader, ParameterOptimizationConfiguration.class);
		try {
			serverIPAddress = InetAddress.getByName(System.getProperty("test_server_ip"));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
 

	

	
	@Test
	@Order(1)
	public void testKubernetes() {
		try {
			System.out.println("-----------------------------------------------------------------------------------------");
			System.out.println("--------------------Starting the testKubernetes test-------------------------------------");
			System.out.println("-----------------------------------------------------------------------------------------");
			Config config = new ConfigBuilder().build();
			KubernetesClient client = new DefaultKubernetesClient(config);
			
			ReplicaSetList list = client.apps().replicaSets().inAnyNamespace().list();
			for (ReplicaSet item : list.getItems()) {
				System.out.println(item.getMetadata().getName());
			}
			client.close();
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	@Order(2)
	public void testCreation() {
		try {
			System.out.println("-----------------------------------------------------------------------------------------");
			System.out.println("--------------------Starting the testCreation test---------------------------------------");
			System.out.println("-----------------------------------------------------------------------------------------");
			SimulationOrchestrator orchestrator = new SimulationOrchestrator(SimulationOrchestrator.OP_MODE.S,
																			serverIPAddress, 
																			serverName, 
																			serverUsername, 
																			serverPassword, 
																			orchestratorInputDataFolder, 
																			orchestratorOutputDataFolder, 
																			optimizationUser, 
																			recovery, "emergency_exit", 
																			guiEnabled, 
																			parameters, 
																			dimensions, 
																			maxAgents, 
																			true, 
																			configurationFolder, 
																			localOptimization, 
																			optimizationToolPath, 
																			optimizationToolPassword, 
																			localSimulationManager, 
																			simulationManagerPath, 
																			optimizationConfiguration, 
																			Boolean.FALSE, 
																			startingTimeout,
																			null,
																			null,
																			null);
			Assert.assertNotNull(orchestrator);
			do {
				Thread.sleep(1000);
			}while(!orchestrator.getConnection().isConnected());
			DummyManager manager = new DummyManager("manager_bamboo", serverIPAddress, serverName, "server", managerDataFolder, rosFolder);
			Assert.assertNotNull(manager);
			Thread.sleep(10000);
			final Roster roster = Roster.getInstanceFor(orchestrator.getConnection());
			RosterEntry entry = roster.getEntry(JidCreate.bareFrom("manager_bamboo@"+serverName));
			Assert.assertNotNull(entry);
			orchestrator.getConnection().disconnect();
			manager.getConnection().disconnect();
		} catch (Exception e) {
			Assert.fail();
		}  
	}
	
	
	@Test
	@Order(3)
	public void testRunSimulation() {	
		try {
			System.out.println("-----------------------------------------------------------------------------------------");
			System.out.println("--------------------Starting the testRunSimulation test----------------------------------");
			System.out.println("-----------------------------------------------------------------------------------------");
			Gson gson = new Gson();
			StatusSerializer serializer = new StatusSerializer();
			SimulationManagerStatus status = serializer.fromJson("{\r\n" + 
					"	\"type\": \"SimulationManager\",\r\n" + 
					   "	\"SCID\": \"\",\r\n" + 
					   "	\"SID\": 1,\r\n" + 
					   "	\"capabilities\": {\r\n" + 
					   "		\"dimensions\": 2,\r\n" + 
					   "        \"max_agents\": 8\r\n" +
					   "	}\r\n" + 
					   "}\r\n");
			Assert.assertNotNull(status);
			SimulationOrchestrator orchestrator = new SimulationOrchestrator(SimulationOrchestrator.OP_MODE.S,
																			serverIPAddress, 
																			serverName, 
																			serverUsername, 
																			serverPassword, 
																			orchestratorInputDataFolder, 
																			orchestratorOutputDataFolder, 
																			optimizationUser, 
																			recovery, 
																			"emergency_exit",
																			guiEnabled, 
																			parameters,
																			dimensions, 
																			maxAgents, 
																			false, 
																			configurationFolder, 
																			localOptimization, 
																			optimizationToolPath, 
																			optimizationToolPassword, 
																			localSimulationManager,
																			simulationManagerPath,
																			optimizationConfiguration, 
																			Boolean.FALSE, 
																			startingTimeout,
																			null,
																			null,
																			null);
			Assert.assertNotNull(orchestrator);
			do {
				Thread.sleep(10000);
			}while(!orchestrator.getConnection().isConnected());
			DummyManager manager = new DummyManager("manager_bamboo", serverIPAddress, serverName, "server", managerDataFolder, rosFolder);
			Assert.assertNotNull(manager);
			Thread.sleep(1000);
			
			orchestrator.evaluateSimulationManagers(status);
			while(!orchestrator.isSimulationDone()) {
				Thread.sleep(1000);
			}
			Assert.assertTrue(orchestrator.isSimulationDone());
			orchestrator.getConnection().disconnect();
			manager.getConnection().disconnect();
			Thread.sleep(5000);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}  
	}
	
	
	
	
	@Test
	@Order(4)
	public void testRunOptimization() {	
		try {
			System.out.println("-----------------------------------------------------------------------------------------");
			System.out.println("--------------------Starting the testRunOptimization test----------------------------------");
			System.out.println("-----------------------------------------------------------------------------------------");
			StatusSerializer serializer = new StatusSerializer();
			SimulationManagerStatus status = serializer.fromJson("{\r\n" + 
					"	\"type\": \"SimulationManager\",\r\n" +
					   "	\"SCID\": \"\",\r\n" + 
					   "	\"SID\": 1,\r\n" +
					   "	\"capabilities\": {\r\n" + 
					   "		\"dimensions\": 2,\r\n" + 
					   "        \"max_agents\": 8\r\n" +
					   "	}\r\n" + 
					   "}\r\n");
			SimulationOrchestrator orchestrator = new SimulationOrchestrator(SimulationOrchestrator.OP_MODE.S,
																			serverIPAddress, 
																			serverName, 
																			serverUsername, 
																			serverPassword,
																			orchestratorInputDataFolder, 
																			orchestratorOutputDataFolder,
																			optimizationUser, 
																			recovery, 
																			"emergency_exit", 
																			guiEnabled, 
																			parameters, 
																			dimensions, 
																			maxAgents, 
																			true, 
																			configurationFolder, 
																			localOptimization, 
																			optimizationToolPath, 
																			optimizationToolPassword, 
																			localSimulationManager, 
																			simulationManagerPath, 
																			optimizationConfiguration, 
																			Boolean.FALSE, 
																			startingTimeout,
																			null,
																			null,
																			null);
			Assert.assertNotNull(orchestrator);
			do {
				Thread.sleep(10000);
			}while(!orchestrator.getConnection().isConnected());
			DummyManager manager = new DummyManager("manager_bamboo", 
					serverIPAddress, serverName, "server", managerDataFolder, rosFolder);
			Assert.assertNotNull(manager);
			DummyOptimizationTool optimizationTool = new DummyOptimizationTool(optimizationUser, serverIPAddress, serverName, "server", otDataFolder);
			Assert.assertNotNull(optimizationTool);
			Thread.sleep(1000);
			//  how to proceed that the data folder is null, the file transfer can not be successfully, so it never set simulation done, ==> dead block for waiting
			orchestrator.evaluateSimulationManagers(status);
			while(!orchestrator.isSimulationDone()) {  // right: after a while value +=10, SOO directly receives a status=COMPLETED, it will set simulation is done 
				Thread.sleep(1000);
			}
			Assert.assertTrue(orchestrator.isSimulationDone());
			orchestrator.getConnection().disconnect();
			manager.getConnection().disconnect();
			optimizationTool.getConnection().disconnect();
			Thread.sleep(5000);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}  
	}
	

	@Test
	@Order(5)
	public void testAddSimulationManager() {	
		try {
			System.out.println("-----------------------------------------------------------------------------------------");
			System.out.println("--------------------Starting the testAddSimulationManager test----------------------------------");
			System.out.println("-----------------------------------------------------------------------------------------");
			StatusSerializer serializer = new StatusSerializer();
			SimulationManagerStatus status = serializer.fromJson("{\r\n" + 
					"	\"type\": \"SimulationManager\",\r\n" +
					   "	\"SCID\": \"\",\r\n" + 
					   "	\"SID\": 1,\r\n" +
					   "	\"capabilities\": {\r\n" + 
					   "		\"dimensions\": 2,\r\n" + 
					   "        \"max_agents\": 8\r\n" +
					   "	}\r\n" + 
					   "}\r\n");
			SimulationOrchestrator orchestrator = new SimulationOrchestrator(SimulationOrchestrator.OP_MODE.S,
																			serverIPAddress, 
																			serverName, 
																			serverUsername, 
																			serverPassword,
																			orchestratorInputDataFolder, 
																			orchestratorOutputDataFolder,
																			optimizationUser, 
																			recovery, 
																			"emergency_exit", 
																			guiEnabled, 
																			parameters, 
																			dimensions, 
																			maxAgents, 
																			true, 
																			configurationFolder, 
																			localOptimization, 
																			optimizationToolPath, 
																			optimizationToolPassword, 
																			localSimulationManager, 
																			simulationManagerPath, 
																			optimizationConfiguration, 
																			Boolean.FALSE, 
																			startingTimeout,
																			null,
																			null,
																			null);
			Assert.assertNotNull(orchestrator);
			do {
				Thread.sleep(10000);
			}while(!orchestrator.getConnection().isConnected());
			DummyManager manager = new DummyManager("manager_bamboo", 
					serverIPAddress, serverName, "server", managerDataFolder, rosFolder);
			Assert.assertNotNull(manager);
			DummyOptimizationTool optimizationTool = new DummyOptimizationTool(optimizationUser, serverIPAddress, serverName, "server", otDataFolder);
			Assert.assertNotNull(optimizationTool);
			Thread.sleep(1000);
			//  how to proceed that the data folder is null, the file transfer can not be successfully, so it never set simulation done, ==> dead block for waiting
			orchestrator.evaluateSimulationManagers(status);
			while(!orchestrator.isSimulationDone()) {  // right: after a while value +=10, SOO directly receives a status=COMPLETED, it will set simulation is done 
				Thread.sleep(1000);
			}
			Assert.assertTrue(orchestrator.isSimulationDone());
			DummyManager manager2 = new DummyManager("manager2_bamboo", 
					serverIPAddress, serverName, "server", managerDataFolder, rosFolder);
			Assert.assertNotNull(manager2);
			
			while(!orchestrator.isNewManagerHandled()) {
				Thread.sleep(1000);
			}
			
			Assert.assertTrue(orchestrator.getAvailableManagers().size()==2);
			
			orchestrator.getConnection().disconnect();
			manager.getConnection().disconnect();
			optimizationTool.getConnection().disconnect();
			Thread.sleep(5000);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}  
	}

	
	
	@Test
	@Order(6)
	public void testOptimizationToolRecoveryConnection() {	
		try {
			System.out.println("-----------------------------------------------------------------------------------------");
			System.out.println("--------------------Starting the testOptimizationToolRecovery1 test----------------------------------");
			System.out.println("-----------------------------------------------------------------------------------------");
			Gson gson = new Gson();
			StatusSerializer serializer = new StatusSerializer();
			SimulationManagerStatus status = serializer.fromJson("{\r\n" + 
					"	\"type\": \"SimulationManager\",\r\n" +
					   "	\"SCID\": \"\",\r\n" + 
					   "	\"SID\": 1,\r\n" +
					   "	\"capabilities\": {\r\n" + 
					   "		\"dimensions\": 2,\r\n" + 
					   "        \"max_agents\": 8\r\n" +
					   "	}\r\n" + 
					   "}\r\n");
			SimulationOrchestrator orchestrator = new SimulationOrchestrator(SimulationOrchestrator.OP_MODE.S,
																			serverIPAddress, 
																			serverName, 
																			serverUsername, 
																			serverPassword,
																			orchestratorInputDataFolder,
																			orchestratorOutputDataFolder,
																			optimizationUser, 
																			recovery,
																			"cpswarm_sar", // This uses cpswarm_sar and not emergency_exit to indicated that need a test optimization that doesn't finish immediately, to test the OT recovery
																			guiEnabled, 
																			parameters, 
																			dimensions, 
																			maxAgents, 
																			true, 
																			configurationFolder, 
																			localOptimization, 
																			optimizationToolPath, 
																			optimizationToolPassword,
																			localSimulationManager, 
																			simulationManagerPath, 
																			optimizationConfiguration, 
																			Boolean.FALSE, 
																			startingTimeout,
																			null,
																			null,
																			null);
			Assert.assertNotNull(orchestrator);
			do {
				Thread.sleep(10000);
			}while(!orchestrator.getConnection().isConnected());
			DummyManager manager = new DummyManager("manager_bamboo", serverIPAddress, serverName, "server", managerDataFolder, rosFolder);
			Assert.assertNotNull(manager);
			DummyOptimizationTool optimizationTool = new DummyOptimizationTool(optimizationUser, serverIPAddress, serverName, "server", otDataFolder);
			Assert.assertNotNull(optimizationTool);
			Thread.sleep(1000);
			//  how to proceed that the data folder is null, the file transfer can not be successfully, so it never set simulation done, ==> dead block for waiting
			orchestrator.evaluateSimulationManagers(status);
			Thread.sleep(15000);
			optimizationTool.disconnect(false);  // immediately stop optimization after connection recovery
			Thread.sleep(10000);
			optimizationTool.reconnect();
			while(!orchestrator.isSimulationDone()) {  // right: after a while value +=10, SOO directly receives a status=COMPLETED, it will set simulation is done 
				Thread.sleep(1000);
			}
			Assert.assertTrue(orchestrator.isSimulationDone());
			orchestrator.getConnection().disconnect();
			manager.getConnection().disconnect();
			optimizationTool.getConnection().disconnect();
			Thread.sleep(5000);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}  
	}
	
	@Test
	@Order(7)
	public void testOptimizationToolRecoveryError() {	
		try {
			System.out.println("-----------------------------------------------------------------------------------------");
			System.out.println("--------------------Starting the testOptimizationToolRecovery2 test----------------------------------");
			System.out.println("-----------------------------------------------------------------------------------------");
			StatusSerializer serializer = new StatusSerializer();
			SimulationManagerStatus status = serializer.fromJson("{\r\n" + 
					"	\"type\": \"SimulationManager\",\r\n" +
					   "	\"SCID\": \"\",\r\n" + 
					   "	\"SID\": 1,\r\n" +
					   "	\"capabilities\": {\r\n" + 
					   "		\"dimensions\": 2,\r\n" + 
					   "        \"max_agents\": 8\r\n" +
					   "	}\r\n" + 
					   "}\r\n");
			SimulationOrchestrator orchestrator = new SimulationOrchestrator(SimulationOrchestrator.OP_MODE.S,
																			serverIPAddress, 
																			serverName, 
																			serverUsername, 
																			serverPassword,
																			orchestratorInputDataFolder,
																			orchestratorOutputDataFolder,
																			optimizationUser, 
																			recovery,
																			"cpswarm_sar", // This usws cpswarm_sar and not emergency_exit to indicated that neeed a test optimization that doesn't finish immediately, to test the OT recovery
																			guiEnabled, 
																			parameters, 
																			dimensions, 
																			maxAgents, 
																			true, 
																			configurationFolder, 
																			localOptimization, 
																			optimizationToolPath, 
																			optimizationToolPassword,
																			localSimulationManager, 
																			simulationManagerPath, 
																			optimizationConfiguration, 
																			Boolean.FALSE, 
																			startingTimeout,
																			null,
																			null,
																			null);
			Assert.assertNotNull(orchestrator);
			do {
				Thread.sleep(10000);
			}while(!orchestrator.getConnection().isConnected());
			DummyManager manager = new DummyManager("manager_bamboo", serverIPAddress, serverName, "server", managerDataFolder, rosFolder);
			Assert.assertNotNull(manager);
			DummyOptimizationTool optimizationTool = new DummyOptimizationTool(optimizationUser, serverIPAddress, serverName, "server", otDataFolder);
			Assert.assertNotNull(optimizationTool);
			Thread.sleep(1000);
			//  how to proceed that the data folder is null, the file transfer can not be successfully, so it never set simulation done, ==> dead block for waiting
			orchestrator.evaluateSimulationManagers(status);
			Thread.sleep(15000);
			optimizationTool.disconnect(true);
			Thread.sleep(10000);
			optimizationTool.reconnect();
			while(!orchestrator.isSimulationDone()) {  // right: after a while value +=10, SOO directly receives a status=COMPLETED, it will set simulation is done 
				Thread.sleep(1000);
			}
			Assert.assertTrue(orchestrator.isSimulationDone());
			orchestrator.getConnection().disconnect();
			manager.getConnection().disconnect();
			optimizationTool.getConnection().disconnect();
			Thread.sleep(5000);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}  
	}
	
	
}