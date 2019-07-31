package simulation;

import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.junit.Assert;
import org.junit.Test;
import org.jxmpp.jid.impl.JidCreate;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import config.frevo.FrevoConfiguration;
/*import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.api.model.apps.ReplicaSetList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;*/
import junit.framework.TestCase;
import messages.server.Server;
import simulation.SimulationOrchestrator;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase{
	/**
	 * Test running example
	 * 
	 * mvn test 
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
	 * -Dmonitoring=true (indicates if the monitoring GUI has to be used, monitoring the evolution of the optimization)
	 * -Dmqtt_broker=tcp://130.192.86.237:1883  (IP of the MQTT broker to be used for the monitoring)
	 * -Dgui_enabled=false (indicates if the GUI has to be used during the simulations)
	 * -Dscid=emergency_exit (ID of the simulator configuration AKA the name of the package)
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
	private String serverIP = System.getProperty("test_server_ip");
	private String serverName = System.getProperty("test_server_name");
	private String serverUsername = System.getProperty("test_server_username");
	private String serverPassword = System.getProperty("test_server_password");
	private String orchestratorInputDataFolder = System.getProperty("test_orchestrator_input_data_folder");
	private String orchestratorOutputDataFolder = System.getProperty("test_orchestrator_output_data_folder");
	private String managerDataFolder = System.getProperty("test_manager_data_folder");
	private String optimizationUser = System.getProperty("optimization_user");
	private String otDataFolder = System.getProperty("ot_data_folder");
	private String rosFolder = System.getProperty("ros_folder");
	private Boolean monitoring = Boolean.parseBoolean(System.getProperty("monitoring"));
	private String mqttBroker = System.getProperty("mqtt_broker");
	private String scid = System.getProperty("scid");
	private Boolean guiEnabled = Boolean.parseBoolean(System.getProperty("gui_enabled"));
	private String parameters = System.getProperty("parameters");
	private String dimensions = System.getProperty("dimensions");
	private Long maxAgents = Long.valueOf(System.getProperty("max_agents"));
	private String configurationFolder = System.getProperty("conf_folder");
	private Boolean localOptimization = Boolean.parseBoolean(System.getProperty("local_optimization"));
	private String optimizationToolPath = System.getProperty("optimzation_tool_path");
	private String optimizationToolPassword = System.getProperty("optimization_tool_password");
	private Boolean localSimulationManager = Boolean.parseBoolean(System.getProperty("local_simulation_manager"));
	private String simulationManagerPath = System.getProperty("simulation_manager_path");
	private static FrevoConfiguration optimizationConfiguration = null;
	private int startingTimeout = Integer.parseInt(System.getProperty("starting_timeout"));
	private static InetAddress serverIPAddress = null;
	static {
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new InputStreamReader(SimulationOrchestrator.class.getResourceAsStream("/frevoConfiguration.json")));
		optimizationConfiguration = gson.fromJson(reader, FrevoConfiguration.class);
		try {
			serverIPAddress = InetAddress.getByName(System.getProperty("test_server_ip"));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
 
/*	@Test
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

	*/

	@Test
	public void testCreation() {
		try {
			System.out.println("-----------------------------------------------------------------------------------------");
			System.out.println("--------------------Starting the testCreation test---------------------------------------");
			System.out.println("-----------------------------------------------------------------------------------------");
			SimulationOrchestrator orchestrator = new SimulationOrchestrator(SimulationOrchestrator.OP_MODE.R,serverIPAddress, serverName, serverUsername, serverPassword, orchestratorInputDataFolder, orchestratorOutputDataFolder, optimizationUser, monitoring, scid, guiEnabled, parameters, dimensions, maxAgents, true, configurationFolder, localOptimization, optimizationToolPath, optimizationToolPassword, localSimulationManager, simulationManagerPath, optimizationConfiguration, Boolean.FALSE, startingTimeout);
			Assert.assertNotNull(orchestrator);
			do {
				Thread.sleep(1000);
			}while(!orchestrator.getConnection().isConnected());
	//		DummyManager manager = new DummyManager("manager_test", serverIPAddress, serverName, "server", managerDataFolder, rosFolder, scid);
			DummyManager manager = new DummyManager("manager_test", serverIPAddress, serverName, "server", managerDataFolder, rosFolder/*, scid*/);
			Assert.assertNotNull(manager);
			Thread.sleep(10000);
			final Roster roster = Roster.getInstanceFor(orchestrator.getConnection());
			RosterEntry entry = roster.getEntry(JidCreate.bareFrom("manager_test@"+serverName));
			Assert.assertNotNull(entry);
			orchestrator.getConnection().disconnect();
			manager.getConnection().disconnect();
		} catch (Exception e) {
			Assert.fail();
		}  
	}
	
	@Test
	public void testRunSimulation() {	
		try {
			System.out.println("-----------------------------------------------------------------------------------------");
			System.out.println("--------------------Starting the testRunSimulation test----------------------------------");
			System.out.println("-----------------------------------------------------------------------------------------");
			Gson gson = new Gson();
			Server server = gson.fromJson("{\r\n" + 
					   "	\"server\": 1,\r\n" + 
//					   "	\"simulation_hash\": \"21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4\",\r\n" + 
					   "	\"SCID\": \"\",\r\n" + 
					   "	\"capabilities\": {\r\n" + 
					   "		\"dimensions\": 2,\r\n" + 
					   "        \"max_agents\": 8\r\n" +
					   "	}\r\n" + 
					   "}\r\n", Server.class);
			SimulationOrchestrator orchestrator = new SimulationOrchestrator(SimulationOrchestrator.OP_MODE.R,serverIPAddress, serverName, serverUsername, serverPassword, orchestratorInputDataFolder, orchestratorOutputDataFolder, optimizationUser, monitoring, scid, guiEnabled, parameters, dimensions, maxAgents, false, configurationFolder, localOptimization, optimizationToolPath, optimizationToolPassword, localSimulationManager, simulationManagerPath, optimizationConfiguration, Boolean.FALSE, startingTimeout);
			Assert.assertNotNull(orchestrator);
			do {
				Thread.sleep(10000);
			}while(!orchestrator.getConnection().isConnected());
		//	DummyManager manager = new DummyManager("manager_test", serverIPAddress, serverName, "server", managerDataFolder, rosFolder, scid);
			DummyManager manager = new DummyManager("manager_test", serverIPAddress, serverName, "server", managerDataFolder, rosFolder/*, scid*/);  // optimizationID is set when transfer()
			Thread.sleep(1000);
			
			orchestrator.evaluateSimulationManagers(server);
			while(orchestrator.isSimulationDone()==null) {  // right: after the dummy manager get RunSimulation from SOO, it directly replys with a SimulationResult=100, SOO will set simulation is done 
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
	public void testRunOptimization() {	
		try {
			System.out.println("-----------------------------------------------------------------------------------------");
			System.out.println("--------------------Starting the testRunOptimization test----------------------------------");
			System.out.println("-----------------------------------------------------------------------------------------");
			Gson gson = new Gson();
			Server server = gson.fromJson("{\r\n" + 
					   "	\"server\": 1,\r\n" + 
//					   "	\"simulation_hash\": \"21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4\",\r\n" + 
					   "	\"SCID\": \"\",\r\n" + 
					   "	\"capabilities\": {\r\n" + 
					   "		\"dimensions\": 2,\r\n" + 
					   "        \"max_agents\": 8\r\n" +
					   "	}\r\n" + 
					   "}\r\n", Server.class);
			SimulationOrchestrator orchestrator = new SimulationOrchestrator(SimulationOrchestrator.OP_MODE.R,serverIPAddress, serverName, serverUsername, serverPassword, orchestratorInputDataFolder, orchestratorOutputDataFolder, optimizationUser, monitoring, scid, guiEnabled, parameters, dimensions, maxAgents, true, configurationFolder, localOptimization, optimizationToolPath, optimizationToolPassword, localSimulationManager, simulationManagerPath, optimizationConfiguration, Boolean.FALSE, startingTimeout);
			Assert.assertNotNull(orchestrator);
			do {
				Thread.sleep(10000);
			}while(!orchestrator.getConnection().isConnected());
		//	DummyManager manager = new DummyManager("manager_test", serverIPAddress, serverName, "server", managerDataFolder, rosFolder, scid);
			DummyManager manager = new DummyManager("manager_test", serverIPAddress, serverName, "server", managerDataFolder, rosFolder/*, scid*/);
		//	DummyOptimizationTool optimizationTool = new DummyOptimizationTool(serverIPAddress, serverName, "server", otDataFolder, scid);
			DummyOptimizationTool optimizationTool = new DummyOptimizationTool(serverIPAddress, serverName, "server", otDataFolder/*, scid*/);
			Thread.sleep(1000);
			//  how to proceed that the data folder is null, the file transfer can not be successfully, so it never set simulation done, ==> dead block for waiting
			orchestrator.evaluateSimulationManagers(server);   // this method is called automatically by SOO, so remove it,
			while(orchestrator.isSimulationDone()==null) {  // right: after a while value +=10, SOO directly receives a status=COMPLETED, it will set simulation is done 
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