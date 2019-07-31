package simulation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
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

import eu.cpswarm.optimization.messages.MessageSerializer;
import eu.cpswarm.optimization.messages.SimulatorConfiguredMessage;

public class ManagerFileTransferListenerImpl implements FileTransferListener {

	private String dataFolder = null;
	private String rosFolder = null;
	private DummyManager parent = null;
	private EntityBareJid orchestrator = null;
	
	public ManagerFileTransferListenerImpl(final DummyManager manager, final String dataFolder, final String rosFolder, final EntityBareJid orchestrator) {
		this.dataFolder = dataFolder;
		this.rosFolder = rosFolder;
		this.parent = manager;
		this.orchestrator = orchestrator;
	}
	
	@Override
	public void fileTransferRequest(FileTransferRequest request) {
		final IncomingFileTransfer transfer = request.accept();
		String fileToReceive = null;
		System.out.println(" description in transfer() is: "+request.getDescription());
		// The configuration files are stored in the simulator folder, instead the candidate in the rosFolder
		if(request.getRequestor().toString().startsWith("orchestrator")) {
			fileToReceive = dataFolder+request.getFileName();
		} else {
			fileToReceive = rosFolder + request.getFileName();
		}
		try {
			transfer.receiveFile(new File(fileToReceive));

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
				if(dataFolder==null || rosFolder==null || unzipFiles(fileToReceive)) {
					System.out.println("SimulationManager configured for optimization "+request.getDescription());
					String otherSimulationConfiguration = request.getDescription();  // Format is: OID,SCID,visual:=false,....
					String[] simConfigs = otherSimulationConfiguration.split(",");
					this.parent.setOptimizationID(simConfigs[0]);
					this.parent.setSCID(simConfigs[1]);
					String parameters = "";
					for(int i=2; i<Arrays.asList(simConfigs).size(); i++) {
						parameters += simConfigs[i];
					}			
					this.parent.setSimulationConfiguration(parameters);	
					SimulatorConfiguredMessage reply = new SimulatorConfiguredMessage(parent.getOptimizationId(), true);
					MessageSerializer serializer = new MessageSerializer();
					newChat.send(serializer.toJson(reply));
				} else {
					System.out.println("Error configuring the simulation manager");
					SimulatorConfiguredMessage reply = new SimulatorConfiguredMessage(parent.getOptimizationId(),false);
					MessageSerializer serializer = new MessageSerializer();
					newChat.send(serializer.toJson(reply));
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
}
