package simulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jxmpp.jid.EntityBareJid;

public class ManagerFileTransferListenerImpl implements FileTransferListener {

	private String dataFolder = null;
	private String rosFolder = null;
	private String catkinWS = null;
	private String optimizationId = null;
	private DummyManager parent = null;
	private EntityBareJid orchestrator = null;
	private ArrayList<NavigableMap<Integer,Double>> logs;
	
	public ManagerFileTransferListenerImpl(final DummyManager manager, final String dataFolder, final String rosFolder, final EntityBareJid orchestrator, final String optimizationId) {
		this.dataFolder = dataFolder;
		this.rosFolder = rosFolder;
		this.catkinWS = rosFolder.substring(0,rosFolder.indexOf("src"));
		this.optimizationId = optimizationId;
		this.parent = manager;
		this.orchestrator = orchestrator;
	}
	
	@Override
	public void fileTransferRequest(FileTransferRequest request) {
		final IncomingFileTransfer transfer = request.accept();
		String fileToReceive = null;
		// The configuration files are stored in the simulator folder, instead the candidate in the rosFolder
		if(request.getRequestor().toString().startsWith("orchestrator")) {
			fileToReceive = dataFolder+request.getFileName();
		} else {
			fileToReceive = rosFolder+request.getFileName();
		}
		try {
			transfer.recieveFile(new File(fileToReceive));

			while (!transfer.isDone()) {
				if (transfer.getStatus() == Status.refused) {
					System.out.println("Transfer refused");
				}
				Thread.sleep(1000);
			}
			System.out.println("Simulation Manager "+fileToReceive+" received");
			Thread.sleep(1000);
			// If it's the configuration from the Simulation Orchestrator
			if(request.getRequestor().toString().startsWith("orchestrator")) {
				final ChatManager chatmanager = ChatManager.getInstanceFor(parent.getConnection());
				final Chat newChat = chatmanager.chatWith(orchestrator);
				if(unzipFiles(fileToReceive)) {
					System.out.println("SimulationManager configured for optimization "+request.getDescription());
					parent.setOptimizationID(request.getDescription());
					newChat.send("simulator configured");
				} else {
					System.out.println("Error configuring the simulation manager");
					newChat.send("error");
				}
			// If it's the candidate from the Optimization Tool
			} else if(request.getRequestor().toString().startsWith("optimization")) {
				try { 
					System.out.println("Compiling the package, using /bin/bash "+catkinWS+"ros.sh");
					Process proc = Runtime.getRuntime().exec("/bin/bash "+catkinWS+"ros.sh");
					System.out.println("Compilation launched");
					boolean result = proc.waitFor(2, TimeUnit.MINUTES);
					System.out.println("Compilation finished, "+result);
					if(result) {
						String packageName = optimizationId.substring(0, optimizationId.indexOf("&"));
						System.out.println("Launching the simulation for package: "+packageName);
						proc = Runtime.getRuntime().exec("roslaunch "+packageName+" stage.launch");
						proc.waitFor(40, TimeUnit.SECONDS);
						if(!calcFitness()) {
							parent.setSimulationDone(false);
							System.out.println("Error");
							return;
						}
						parent.setSimulationDone(true);
						System.out.println("done");
					} else {
						System.out.println("Error");
						parent.setSimulationDone(false);
						return;
					}
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				} 
			}
			
		} catch (final SmackException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	private boolean unzipFiles(final String fileToReceive) {
		try {
			System.out.println("Unzipping "+fileToReceive);
			byte[] buffer = new byte[1024];
			ZipInputStream zis = new ZipInputStream(new FileInputStream(fileToReceive));
			ZipEntry zipEntry = zis.getNextEntry();
			while(zipEntry != null){
				String fileName = zipEntry.getName();
				File newFile = null;
				// The wrapper is copied to the ROS folder
				if(fileName.endsWith(".cpp")) {
					newFile = new File(rosFolder + fileName);
				} else {
					newFile = new File(dataFolder + fileName);
				}
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				System.out.println("Unzipped "+newFile);
				fos.close();				
				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}	
		return true;
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
		
		System.out.println("Reading logs from :"+catkinWS + "/src/" + optimizationId + "/log/");
		
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
        parent.publishFitness(-dist);
        
        return true;
	}
}
