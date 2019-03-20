package simulation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.jxmpp.jid.impl.JidCreate;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import config.frevo.FrevoConfiguration;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.AppsV1Api;
import io.kubernetes.client.apis.AppsV1beta1Api;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.AppsV1beta1Deployment;
import io.kubernetes.client.models.AppsV1beta1DeploymentCondition;
import io.kubernetes.client.models.AppsV1beta1DeploymentSpec;
import io.kubernetes.client.models.AppsV1beta1DeploymentStatus;
import io.kubernetes.client.models.AppsV1beta1DeploymentStrategy;
import io.kubernetes.client.models.AppsV1beta1RollingUpdateDeployment;
import io.kubernetes.client.models.ExtensionsV1beta1Deployment;
import io.kubernetes.client.models.ExtensionsV1beta1DeploymentCondition;
import io.kubernetes.client.models.ExtensionsV1beta1DeploymentSpec;
import io.kubernetes.client.models.ExtensionsV1beta1DeploymentStatus;
import io.kubernetes.client.models.ExtensionsV1beta1DeploymentStrategy;
import io.kubernetes.client.models.ExtensionsV1beta1RollingUpdateDeployment;
import io.kubernetes.client.models.ExtensionsV1beta1Scale;
import io.kubernetes.client.models.ExtensionsV1beta1ScaleSpec;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1Deployment;
import io.kubernetes.client.models.V1DeploymentCondition;
import io.kubernetes.client.models.V1DeploymentSpec;
import io.kubernetes.client.models.V1DeploymentStatus;
import io.kubernetes.client.models.V1DeploymentStrategy;
import io.kubernetes.client.models.V1LabelSelector;
import io.kubernetes.client.models.V1LabelSelectorRequirement;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.models.V1PodSecurityContext;
import io.kubernetes.client.models.V1PodSpec;
import io.kubernetes.client.models.V1PodTemplate;
import io.kubernetes.client.models.V1PodTemplateSpec;
import io.kubernetes.client.models.V1ResourceRequirements;
import io.kubernetes.client.models.V1RollingUpdateDeployment;
import io.kubernetes.client.models.V1SecurityContext;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1Status;
import io.kubernetes.client.proto.V1beta1Apiextensions.JSON;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Yaml;
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
	 * -Dtask_id=emergency_exit (ID of the task AKA the name of the package)
	 * -Dparameters="" (indicates the parameters to be used in the simulations)
	 * -Ddimensions = "2D" (indicates the number of dimensions required for the simulation)
	 * -Dmax_agents = "8" (indicates the maximum number of agents required for the simulation)
	 * -Dconfiguration_folder=/home/cpswarm/Desktop/configuration/ (path with the configuration files) -- Optional
	 * -Dlocal_optimization=true (Indicates if the Simulation Orchestator has to launch also the Optimization Tool)
	 * -optimization_tool_path=/home/cpswarm/Desktop/ (path of the executable of the Optimization Tool)
	 * -Doptimization_tool_password = blah  (To be used to launch the optimization tool from the orchestrator)
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
	private String taskId = System.getProperty("task_id");
	private Boolean guiEnabled = Boolean.parseBoolean(System.getProperty("gui_enabled"));
	private String parameters = System.getProperty("parameters");
	private String dimensions = System.getProperty("dimensions");
	private Long maxAgents = Long.valueOf(System.getProperty("max_agents"));
	private String configurationFolder = System.getProperty("conf_folder");
	private Boolean localOptimization = Boolean.parseBoolean(System.getProperty("local_optimization"));
	private String optimizationToolPath = System.getProperty("optimzation_tool_path");
	private String optimizationToolPassword = System.getProperty("optimization_tool_password");
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
 
	
	@Test
	public void testKubernetes() {
		try {
			ApiClient client = Config.defaultClient();
			Configuration.setDefaultApiClient(client);

			CoreV1Api api = new CoreV1Api();
			V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
			for (V1Pod item : list.getItems()) {
				System.out.println(item.getMetadata().getName());
			}
		} catch (IOException | ApiException e) {
			Assert.fail();
		}
	}
	
	

	@Test
	public void testCreation() {
		try {
			System.out.println("-----------------------------------------------------------------------------------------");
			System.out.println("--------------------Starting the testCreation test---------------------------------------");
			System.out.println("-----------------------------------------------------------------------------------------");
			SimulationOrchestrator orchestrator = new SimulationOrchestrator(SimulationOrchestrator.OP_MODE.R,serverIPAddress, serverName, serverUsername, serverPassword, orchestratorInputDataFolder, orchestratorOutputDataFolder, optimizationUser, monitoring, mqttBroker, taskId, guiEnabled, parameters, dimensions, maxAgents, true, configurationFolder, localOptimization, optimizationToolPath, optimizationToolPassword, optimizationConfiguration, Boolean.FALSE, startingTimeout);
			Assert.assertNotNull(orchestrator);
			do {
				Thread.sleep(1000);
			}while(!orchestrator.getConnection().isConnected());
			DummyManager manager = new DummyManager("manager_test", serverIPAddress, serverName, "server", managerDataFolder, rosFolder, taskId);
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
					   "	\"simulation_hash\": \"21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4\",\r\n" + 
					   "	\"simulations\": [\"stage\"],\r\n" + 
					   "	\"capabilities\": {\r\n" + 
					   "		\"dimensions\": 2,\r\n" + 
					   "        \"max_agents\": 8\r\n" +
					   "	}\r\n" + 
					   "}\r\n", Server.class);
			SimulationOrchestrator orchestrator = new SimulationOrchestrator(SimulationOrchestrator.OP_MODE.R,serverIPAddress, serverName, serverUsername, serverPassword, orchestratorInputDataFolder, orchestratorOutputDataFolder, optimizationUser, monitoring, mqttBroker, taskId, guiEnabled, parameters, dimensions, maxAgents, false, configurationFolder, localOptimization, optimizationToolPath, optimizationToolPassword, optimizationConfiguration, Boolean.FALSE, startingTimeout);
			Assert.assertNotNull(orchestrator);
			do {
				Thread.sleep(10000);
			}while(!orchestrator.getConnection().isConnected());
			DummyManager manager = new DummyManager("manager_test", serverIPAddress, serverName, "server", managerDataFolder, rosFolder, taskId);
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
					   "		\"dimensions\": 2,\r\n" + 
					   "        \"max_agents\": 8\r\n" +
					   "	}\r\n" + 
					   "}\r\n", Server.class);
			SimulationOrchestrator orchestrator = new SimulationOrchestrator(SimulationOrchestrator.OP_MODE.R,serverIPAddress, serverName, serverUsername, serverPassword, orchestratorInputDataFolder, orchestratorOutputDataFolder, optimizationUser, monitoring, mqttBroker, taskId, guiEnabled, parameters, dimensions, maxAgents, true, configurationFolder, localOptimization, optimizationToolPath, optimizationToolPassword, optimizationConfiguration, Boolean.FALSE, startingTimeout);
			Assert.assertNotNull(orchestrator);
			do {
				Thread.sleep(10000);
			}while(!orchestrator.getConnection().isConnected());
			DummyManager manager = new DummyManager("manager_test", serverIPAddress, serverName, "server", managerDataFolder, rosFolder, taskId);
			DummyOptimizationTool optimizationTool = new DummyOptimizationTool(serverIPAddress, serverName, "server", otDataFolder, taskId);
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