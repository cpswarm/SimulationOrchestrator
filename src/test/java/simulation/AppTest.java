package simulation;

import org.junit.Test;

import junit.framework.TestCase;
import simulation.SimulationOrchestrator;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase{
	private String serverIP = System.getProperty("test_server_ip");
	private String serverName = System.getProperty("test_server_name");
	private String serverPassword = System.getProperty("test_server_password");
	
   @Test
   public void testCreation() {
	   SimulationOrchestrator orchestrator = new SimulationOrchestrator(serverIP, serverName, serverPassword);
	   DummyManager manager = new DummyManager(serverIP, serverName, serverPassword);
	   while(true) {
		 try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	   }
	   //Assert.assertNotNull(launcher);
	   //Assert.assertNotNull(manager);
   }
 
   /*
   @Test
   public void testConnectionLost() {
	   DummyManager manager = new DummyManager(serverIP, serverName, serverPassword);
	   Assert.assertTrue(manager.getConnection().isConnected());
   }
   
   
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
