package simulation;

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
	private String serverIP = System.getProperty("test_server_ip");
	private String serverName = System.getProperty("test_server_name");
	private String serverPassword = System.getProperty("test_server_password");
	private String orchestratorDataFolder = System.getProperty("test_orchestrator_data_folder");
	private String managerDataFolder = System.getProperty("test_manager_data_folder");
	private String optimizationUser = System.getProperty("optimization_user");
	private String otDataFolder = System.getProperty("ot_data_folder");
	private String rosFolder = System.getProperty("ros_folder");
	private Boolean monitoring = Boolean.parseBoolean(System.getProperty("monitoring"));
	private String mqttBroker = System.getProperty("mqtt_broker");
	
	@Test
	public void testCreation() {
		try {
			SimulationOrchestrator orchestrator = new SimulationOrchestrator(serverIP, serverName, serverPassword, orchestratorDataFolder, optimizationUser, monitoring, mqttBroker);
			Assert.assertNotNull(orchestrator);
			do {
				Thread.sleep(1000);
			}while(!orchestrator.getConnection().isConnected());
			DummyManager manager = new DummyManager(serverIP, serverName, "server", managerDataFolder, rosFolder);
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
	public void testConfiguration() {
		try {
			Gson gson = new Gson();
			Server server = gson.fromJson("{\r\n" + 
					"	\"server\": 1,\r\n" + 
					"	\"simulation_hash\": \"21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4\",\r\n" + 
					"	\"simulations\": [\"minisim\"],\r\n" + 
					"	\"capabilities\": {\r\n" + 
					"		\"dimensions\": 2\r\n" + 
					"	}\r\n" + 
					"}\r\n" + 
					"", Server.class);
			SimulationOrchestrator orchestrator = new SimulationOrchestrator(serverIP, serverName, serverPassword, orchestratorDataFolder, optimizationUser, monitoring, mqttBroker);
			Assert.assertNotNull(orchestrator);
			do {
				Thread.sleep(10000);
			}while(!orchestrator.getConnection().isConnected());
			DummyManager manager = new DummyManager(serverIP, serverName, "server", managerDataFolder, rosFolder);
			DummyOptimizationTool optimizationTool = new DummyOptimizationTool(serverIP, serverName, "server", otDataFolder);
			Thread.sleep(1000);
			
			orchestrator.evaluateSimulationManagers(server);
			while(true) {}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}  
	}

	/*
   @Test
   public void testMessageArrived() {
	   DummyManager manager = new DummyManager(serverIP, serverName, serverPassword);
	   try {
		   Server serverInfo = new Server();

		   serverInfo.setServer(Long.valueOf(37));

		   // define name of this simulation
		   List<String> simulations = new ArrayList<String>();
		   simulations.add("minisim");
		   serverInfo.setSimulations(simulations);

		   // define capabilities of this simulation
		   Capabilities caps = new Capabilities();
		   caps.setDimensions(Long.valueOf(2));
		   caps.setMaxAgents(Long.valueOf(3));
		   serverInfo.setCapabilities(caps);

		   // set server info
		   manager.setServerInfo(serverInfo);


		   ////////////////////////////////
		   //                            // 
		   // Test of control message    //
		   //                            //
		   ////////////////////////////////
		   /*
		   message = new MqttMessage();
		   message.setPayload("{\"server\":37,\"simulation_hash\":\"e28f2\",\"visual\":false,\"run\":true}".getBytes());
		   wrapper.messageArrived("control", message);
		   Assert.assertTrue(wrapper.isStarted());
		   Assert.assertTrue(wrapper.isControlReceived());


	   } catch (Exception ex) {
		   Assert.fail();
	   }
   }



   public void testPublishServer() {
	   DummyManager manager = new DummyManager(serverIP, serverName, serverPassword);
	   Server serverInfo = new Server();

	   serverInfo.setServer(Long.valueOf(37));

	   // define name of this simulation
	   List<String> simulations = new ArrayList<String>();
	   simulations.add("minisim");
	   serverInfo.setSimulations(simulations);

	   // define capabilities of this simulation
	   Capabilities caps = new Capabilities();
	   caps.setDimensions(Long.valueOf(2));
	   caps.setMaxAgents(Long.valueOf(3));
	   serverInfo.setCapabilities(caps);

	   manager.setServerInfo(serverInfo);

	   Assert.assertTrue(manager.publishServer("efgcfgfc5"));
   }

   public void testPublishFitness() {
	   DummyManager wrapper = new DummyManager(serverIP, serverName, serverPassword);
	   Fitness fitness = new Fitness();
	   fitness.setFitness(-2.0);
	   Assert.assertTrue(wrapper.publishFitness(fitness));
   }
	 */
}
