package simulation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
					parent.setOptimizationID(request.getDescription());
					newChat.send("simulator configured");
				} else {
					newChat.send("error");
				}
			// If it's the candidate from the Optimization Tool
			} else if(request.getRequestor().toString().startsWith("optimization")) {
				try { 
					System.out.println("Compiling the package");
					Process proc = Runtime.getRuntime().exec("/bin/bash "+catkinWS+"ros.sh");
					System.out.println("Compilation launched");
					int result = proc.waitFor();
					System.out.println("Compilation finished, "+result);
					if(result == 0) {
						System.out.println("Launching the simulation for package: "+optimizationId);
						proc = Runtime.getRuntime().exec("roslaunch "+optimizationId+" gazebo.launch"+(parent.getGuiEnabled().booleanValue()?"":" gui:=false"));
						System.out.println("done");
					} else {
						System.out.println("Error");
					}
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
			
		} catch (final SmackException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	private boolean unzipFiles(final String fileToReceive) {
		try {
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
}
