package simulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.TreeMap;
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
	 * -Dtest_manager2_data_folder=/home/cpswarm/Desktop/output2/ (data folder used by the second simulation manager for multiple simulations test)
	 * -Doptimization_user=optimization_test (XMPP username used for the Optimization Tool)
	 * -Dot_data_folder=/home/cpswarm/Desktop/ot/  (folder used by the Optimization Tool)
	 * -Dros_folder=/home/cpswarm/Desktop/test/src/emergency_exit/src/ (Folder used for the ROS package to start the first simulation)
	 * -Dros2_folder=/home/cpswarm/Desktop/test2/src/emergency_exit/src/ (Folder used for the ROS package to start the second simulation for multiple simulations test)
	 * -Dmonitoring=true (indicates if the monitoring GUI has to be used, monitoring the evolution of the optimization)
	 * -Dmqtt_broker=tcp://130.192.86.237:1883  (IP of the MQTT broker to be used for the monitoring)
	 * -Dgui_enabled=false (indicates if the GUI has to be used during the simulations)
	 * -Doptimization_id=emergency_exit (ID of the optimization AKA the name of the package)
	 * -Djavax.xml.accessExternalDTD=all (configuration for xml parsing)
	 * 
	 */
	private String serverIP = System.getProperty("test_server_ip");
	private String serverName = System.getProperty("test_server_name");
	private String serverPassword = System.getProperty("test_server_password");
	private String orchestratorInputDataFolder = System.getProperty("test_orchestrator_input_data_folder");
	private String orchestratorOutputDataFolder = System.getProperty("test_orchestrator_output_data_folder");
	private String managerDataFolder = System.getProperty("test_manager_data_folder");
	private String manager2DataFolder = System.getProperty("test_manager2_data_folder");
	private String optimizationUser = System.getProperty("optimization_user");
	private String otDataFolder = System.getProperty("ot_data_folder");
	private String rosFolder = System.getProperty("ros_folder");
	private String ros2Folder = System.getProperty("ros2_folder");
	private Boolean monitoring = Boolean.parseBoolean(System.getProperty("monitoring"));
	private String mqttBroker = System.getProperty("mqtt_broker");
	private String optimizationId = System.getProperty("optimization_id");
	private Boolean guiEnabled = Boolean.parseBoolean(System.getProperty("gui_enabled"));
	private String catkinWS = null;
	private ArrayList<NavigableMap<Integer,Double>> logs;
	
	@Test
	public void testCompilation() {
		System.out.println("-----------------------------------------------------------------------------------------");
		System.out.println("--------------------Starting the testCompilation test------------------------------------");
		System.out.println("-----------------------------------------------------------------------------------------");
		catkinWS = rosFolder.substring(0,rosFolder.indexOf("src"));
		try { 
			System.out.println("Compiling the package, using /bin/bash "+catkinWS+"ros.sh");
			Process proc = Runtime.getRuntime().exec("/bin/bash "+catkinWS+"ros.sh");
			System.out.println("Compilation launched");
			int result = proc.waitFor();
			System.out.println("Compilation finished, "+result);
			if(result == 0) {
				System.out.println("Launching the simulation for package: "+optimizationId);
				proc = Runtime.getRuntime().exec("roslaunch "+optimizationId+" stage.launch");
				boolean value = false;
				value = proc.waitFor(40, TimeUnit.SECONDS);
				calcFitness();
				System.out.println("done");
			} else {
				System.out.println("Error");
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Read the log files produced by ROS.
	 * It assumes log files with two columns, separated by tabulator.
	 * The first column must be an integer, the second a double value.
	 * @return ArrayList<NavigableMap<Integer,Double>>: An array with one map entry for each log file.
	 */
	private boolean readLogs() {
		// container for data of all log files
		logs = new ArrayList<NavigableMap<Integer,Double>>();
		
		System.out.println("Reading logs from "+catkinWS + "/src/" + optimizationId + "/log/");
		
		// path to log directory
	    File logPath = new File(catkinWS + "/src/" + optimizationId + "/log/");
	    
	    // iterate through all log files
	    String[] logFiles = logPath.list();
	    for ( int i=0; i<logFiles.length; i++ ) {
	    	// container for data of one log file
	    	NavigableMap<Integer,Double> log = new TreeMap<Integer, Double>();
	    	
	    	// read log file
	    	Path logFile = Paths.get(logPath + "/" + logFiles[i]);
	    	try {
	    		BufferedReader logReader = Files.newBufferedReader(logFile);
	    		
	    		// store every line
		    	String line;
				while ((line = logReader.readLine()) != null) {
					if ( line.length() <= 1 || line.startsWith("#") )
						continue;
					
					log.put(Integer.parseInt(line.split("\t")[0]), Double.parseDouble(line.split("\t")[1]));
				}
			}
	    	catch (IOException e) {
				e.printStackTrace();
			}
	    	
	    	// store contents of log file
	    	logs.add(log);
	    }
	    return true;
	}
	
	/**
	 * Calculate the fitness score of the last simulation run.
	 * @return boolean: result of the method.
	 */
	private boolean calcFitness() {

		if(!readLogs()) {
			return false;
		}
		
		// fitness score is negative sum of all distances
		double dist = 0;
		
		// iterate all log files
        for (NavigableMap<Integer,Double> log : logs) {
        	if (log.size() > 0)
	            // take last line of log file
	            dist = dist + log.lastEntry().getValue();
        }

        // publish negative distance as fitness
        System.out.println("Distance: "+(-dist));
        
        return true;
	}
	
	/*
	@Test
	public void testCreation() {
		try {
			System.out.println("-----------------------------------------------------------------------------------------");
			System.out.println("--------------------Starting the testCreation test---------------------------------------");
			System.out.println("-----------------------------------------------------------------------------------------");
			SimulationOrchestrator orchestrator = new SimulationOrchestrator(serverIP, serverName, serverPassword, orchestratorInputDataFolder, orchestratorOutputDataFolder, optimizationUser, monitoring, mqttBroker, optimizationId, guiEnabled);
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

	@Test
	public void testConfiguration() {
		try {
			System.out.println("-----------------------------------------------------------------------------------------");
			System.out.println("--------------------Starting the testConfiguration test----------------------------------");
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
			SimulationOrchestrator orchestrator = new SimulationOrchestrator(serverIP, serverName, serverPassword, orchestratorInputDataFolder, orchestratorOutputDataFolder, optimizationUser, monitoring, mqttBroker, optimizationId, guiEnabled);
			Assert.assertNotNull(orchestrator);
			do {
				Thread.sleep(10000);
			}while(!orchestrator.getConnection().isConnected());
			DummyManager manager = new DummyManager("manager_test", serverIP, serverName, "server", managerDataFolder, rosFolder, optimizationId);
			DummyOptimizationTool optimizationTool = new DummyOptimizationTool(serverIP, serverName, "server", otDataFolder, optimizationId);
			Thread.sleep(1000);
			
			orchestrator.evaluateSimulationManagers(server);
			while(manager.isSimulationDone()==null) {
				Thread.sleep(1000);
			}
			Assert.assertTrue(manager.isSimulationDone());
			orchestrator.getConnection().disconnect();
			manager.getConnection().disconnect();
			optimizationTool.getConnection().disconnect();
			Thread.sleep(5000);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}  
	}
	*/
	
	@Test
	public void testMultiConfiguration() {
		try {
			System.out.println("-----------------------------------------------------------------------------------------");
			System.out.println("--------------------Starting the testMultiConfiguration test-----------------------------");
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
			SimulationOrchestrator orchestrator = new SimulationOrchestrator(serverIP, serverName, serverPassword, orchestratorInputDataFolder, orchestratorOutputDataFolder, optimizationUser, monitoring, mqttBroker, optimizationId, guiEnabled);
			Assert.assertNotNull(orchestrator);
			do {
				Thread.sleep(10000);
			}while(!orchestrator.getConnection().isConnected());
			DummyManager manager = new DummyManager("manager_test", serverIP, serverName, "server", managerDataFolder, rosFolder, optimizationId);
			DummyManager manager2 = new DummyManager("manager_test2", serverIP, serverName, "server", manager2DataFolder, ros2Folder, optimizationId);
			DummyOptimizationTool optimizationTool = new DummyOptimizationTool(serverIP, serverName, "server", otDataFolder, optimizationId);
			Thread.sleep(1000);
			
			orchestrator.evaluateSimulationManagers(server);
			while(manager.isSimulationDone()==null || manager2.isSimulationDone()==null) {
				Thread.sleep(1000);
			}
			Assert.assertTrue(manager.isSimulationDone());
			Assert.assertTrue(manager2.isSimulationDone());
			orchestrator.getConnection().disconnect();
			manager.getConnection().disconnect();
			manager2.getConnection().disconnect();
			optimizationTool.getConnection().disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}  
	}
}
