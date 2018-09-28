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

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import eu.cpswarm.optimization.messages.MessageSerializer;
import eu.cpswarm.optimization.messages.ReplyMessage.Status;
import eu.cpswarm.optimization.messages.RunSimulationMessage;
import eu.cpswarm.optimization.messages.SimulationResultMessage;

/**
 *
 * The standard implementation of <code>MessageEventCoordinator</code>
 *
 */
public final class ManagerMessageEventCoordinatorImpl implements IncomingChatMessageListener {
	private DummyManager parent = null;
	private String rosFolder = null;
	private String catkinWS = null;
	private String optimizationId = null;
	private String packageName = null;
	private ArrayList<NavigableMap<Integer,Double>> logs;
	private static final Double BAD_FITNESS = 0.0;
	
	public ManagerMessageEventCoordinatorImpl(final DummyManager manager, final String rosFolder, final String optimizationId) {
		this.parent = manager;
		this.rosFolder = rosFolder;
		this.catkinWS = rosFolder.substring(0,rosFolder.indexOf("src"));
		this.optimizationId = optimizationId;
	}
	
	@Override
	public void newIncomingMessage(EntityBareJid sender, Message msg, org.jivesoftware.smack.chat2.Chat chat) {
		if(sender.toString().startsWith("optimization")) {
			MessageSerializer serializer = new MessageSerializer();
			RunSimulationMessage runSimulation = serializer.fromJson(msg.getBody());
			System.out.println("SimulationManager received "+msg.getBody());
			parent.setOptimizationID(runSimulation.getId());
			parent.setSimulationId(runSimulation.getSid());
			parent.setSimulationConfiguration(runSimulation.getConfiguration());
			if(!serializeCandidate(runSimulation.getCandidate())) {
				parent.publishFitness(BAD_FITNESS);
				return;
			}
			System.out.println("Compiling the package, using /bin/bash "+catkinWS+"ros.sh");
			Process proc;
			try {
				proc = Runtime.getRuntime().exec("/bin/bash "+catkinWS+"ros.sh");
				System.out.println("Compilation launched");
				boolean result = proc.waitFor(2, TimeUnit.MINUTES);
				System.out.println("Compilation finished, "+result);
				if(result) {
					packageName = parent.getOptimizationId().substring(0, optimizationId.indexOf(":"));
					System.out.println("Launching the simulation for package: "+packageName);
					proc = Runtime.getRuntime().exec("roslaunch "+packageName+" stage.launch");
					proc.waitFor(40, TimeUnit.SECONDS);
					if(!calcFitness()) {
						System.out.println("Error");
						return;
					}
					System.out.println("done");
				} else {
					System.out.println("Error");
					return;
				}
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(sender.toString().startsWith("orchestrator")) {
			SimulationResultMessage message = new SimulationResultMessage("test", "simulation result", Status.OK, "1", 100);
			MessageSerializer serializer = new MessageSerializer();
			Message messageToSend = new Message();
			messageToSend.setBody(serializer.toJson(message));
			try {
				chat.send(messageToSend);
			} catch (NotConnectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	private boolean serializeCandidate(final String candidate) {
		try {
			Files.write(Paths.get(rosFolder+"candidate.c"), candidate.getBytes());
			return true;
		} catch (IOException e) {
			System.out.println("Error serializing the file");
			e.printStackTrace();
			return false;
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
		
		System.out.println("Reading logs from :"+catkinWS + "/src/" + packageName + "/log/");
		
		// path to log directory
	    File logPath = new File(catkinWS + "/src/" + packageName + "/log/");
	    
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
			System.out.println("Error reading logs");
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
        
        System.out.println("Fitness score calculated: "+(-dist)+", ready to be sent");
        
        // publish negative distance as fitness
        parent.publishFitness(100-dist);
        
        return true;
	}
}
