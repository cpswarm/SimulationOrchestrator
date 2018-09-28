package simulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.junit.Assert;
import org.junit.Test;
import org.jxmpp.jid.impl.JidCreate;

import com.google.gson.Gson;

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
	 * -Dtest_server_password=orchestrator (Password to be used by the orchestrator to authenticate in the XMPP server)
	 * -Dtest_orchestrator_input_data_folder=/home/cpswarm/Desktop/cpswarm/ (folder containing the input files)
	 * -Dtest_orchestrator_output_data_folder=/home/cpswarm/Desktop/cpswarm-out (folder where the output files will be inserted)
	 * -Dtest_manager_data_folder=/home/cpswarm/Desktop/output/ (data folder used by the simulation manager) 
	 * -Doptimization_user=optimization_test (XMPP username used for the Optimization Tool)
	 * -Dot_data_folder=/home/cpswarm/Desktop/ot/  (folder used by the Optimization Tool)
	 * -Dros_folder=/home/cpswarm/Desktop/test/src/emergency_exit/src/ (Folder used for the ROS package to start the first simulation)
	 * -Dmonitoring=true (indicates if the monitoring GUI has to be used, monitoring the evolution of the optimization)
	 * -Dmqtt_broker=tcp://130.192.86.237:1883  (IP of the MQTT broker to be used for the monitoring)
	 * -Dgui_enabled=false (indicates if the GUI has to be used during the simulations)
	 * -Dtask_id=emergency_exit (ID of the task AKA the name of the package)
	 * -Dparameters="" (indicates the parameters to be used in the simulations)
	 * -Ddimensions = "2D" (indicates the number of dimensions required for the simulation)
	 * -Dmax_agents = "8" (indicates the maximum number of agents required for the simulation)
	 * -Dconfiguration_folder=/home/cpswarm/Desktop/configuration/ path with the configuration files
	 * -Dlocal_optimization=true Indicates if the Simulation Orhcestator has to launch also the Optimization Tool
	 * -optimization_tool_path=/home/cpswarm/Desktop/ path of the executable of the Optimization Tool
	 * -Doptimization_tool_password = blah  To be used to launch the optimization tool from the orchestrator (localOptimization = true)
	 * -Djavax.xml.accessExternalDTD=all (configuration for xml parsing)
	 * 
	 */
	private String serverIP = System.getProperty("test_server_ip");
	private String serverName = System.getProperty("test_server_name");
	private String serverPassword = System.getProperty("test_server_password");
	private String orchestratorInputDataFolder = System.getProperty("test_orchestrator_input_data_folder");
	private String orchestratorOutputDataFolder = System.getProperty("test_orchestrator_output_data_folder");
	private String managerDataFolder = System.getProperty("test_manager_data_folder");
	private String optimizationUser = System.getProperty("optimization_user");
	private String otDataFolder = System.getProperty("ot_data_folder");
	private String rosFolder = System.getProperty("ros_folder");
	private Boolean monitoring = Boolean.parseBoolean(System.getProperty("monitoring"));
	private String mqttBroker = System.getProperty("mqtt_broker");
	private String optimizationId = System.getProperty("optimization_id") + ":" + UUID.randomUUID();
	private Boolean guiEnabled = Boolean.parseBoolean(System.getProperty("gui_enabled"));
	private String parameters = System.getProperty("parameters");
	private String dimensions = System.getProperty("dimensions");
	private Long maxAgents = Long.valueOf(System.getProperty("max_agents"));
	private String configurationFolder = System.getProperty("conf_folder");
	private Boolean localOptimization = Boolean.parseBoolean(System.getProperty("local_optimization"));
	private String optimizationToolPath = System.getProperty("optimzation_tool_path");
	private String optimizationToolPassword = System.getProperty("optimization_tool_password");

	/*
	@Test
	public void testCreation() {
		try {
			System.out.println("-----------------------------------------------------------------------------------------");
			System.out.println("--------------------Starting the testCreation test---------------------------------------");
			System.out.println("-----------------------------------------------------------------------------------------");
			SimulationOrchestrator orchestrator = new SimulationOrchestrator(serverIP, serverName, serverPassword, orchestratorInputDataFolder, orchestratorOutputDataFolder, optimizationUser, monitoring, mqttBroker, optimizationId, guiEnabled, parameters, dimensions, maxAgents, true, configurationFolder, localOptimization, optimizationToolPath, optimizationToolPassword);
			Assert.assertNotNull(orchestrator);
			do {
				Thread.sleep(1000);
			}while(!orchestrator.getConnection().isConnected());
			DummyManager manager = new DummyManager("manager_test", serverIP, serverName, "server", managerDataFolder, rosFolder, optimizationId);
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

	*/
	
	@Test
	public void testRunSimulation() {	
		try {
			System.out.println("-----------------------------------------------------------------------------------------");
			System.out.println("--------------------Starting the testRunSimulation test----------------------------------");
			System.out.println("-----------------------------------------------------------------------------------------");
			Gson gson = new Gson();
			Server server = gson.fromJson("{\r\n" + 
					"	\"server\": 1,\r\n" + 
					"	\"simulation_hash\": \"21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4\",\r\n" + 
					"	\"simulations\": [\"stage\"],\r\n" + 
					"	\"capabilities\": {\r\n" + 
					"		\"dimensions\": 2\r\n" + 
					"	}\r\n" + 
					"}\r\n" + 
					"", Server.class);
			SimulationOrchestrator orchestrator = new SimulationOrchestrator(serverIP, serverName, serverPassword, orchestratorInputDataFolder, orchestratorOutputDataFolder, optimizationUser, monitoring, mqttBroker, optimizationId, guiEnabled, parameters, dimensions, maxAgents, false, configurationFolder, localOptimization, optimizationToolPath, optimizationToolPassword);
			Assert.assertNotNull(orchestrator);
			do {
				Thread.sleep(10000);
			}while(!orchestrator.getConnection().isConnected());
			DummyManager manager = new DummyManager("manager_test", serverIP, serverName, "server", managerDataFolder, rosFolder, optimizationId);
			Thread.sleep(1000);
			
			orchestrator.evaluateSimulationManagers(server);
			while(orchestrator.isSimulationDone()==null) {
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
					"	\"simulation_hash\": \"21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4\",\r\n" + 
					"	\"simulations\": [\"stage\"],\r\n" + 
					"	\"capabilities\": {\r\n" + 
					"		\"dimensions\": 2\r\n" + 
					"	}\r\n" + 
					"}\r\n" + 
					"", Server.class);
			SimulationOrchestrator orchestrator = new SimulationOrchestrator(serverIP, serverName, serverPassword, orchestratorInputDataFolder, orchestratorOutputDataFolder, optimizationUser, monitoring, mqttBroker, optimizationId, guiEnabled, parameters, dimensions, maxAgents, true, configurationFolder, localOptimization, optimizationToolPath, optimizationToolPassword);
			Assert.assertNotNull(orchestrator);
			do {
				Thread.sleep(10000);
			}while(!orchestrator.getConnection().isConnected());
			DummyManager manager = new DummyManager("manager_test", serverIP, serverName, "server", managerDataFolder, rosFolder, optimizationId);
			DummyOptimizationTool optimizationTool = new DummyOptimizationTool(serverIP, serverName, "server", otDataFolder, optimizationId);
			Thread.sleep(1000);
			
			orchestrator.evaluateSimulationManagers(server);
			while(orchestrator.isSimulationDone()==null) {
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
