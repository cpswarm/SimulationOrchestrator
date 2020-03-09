package simulation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang.StringUtils;
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
	
	public ManagerFileTransferListenerImpl(final DummyManager manager, final String dataFolder, final String rosFolder) {
		this.dataFolder = dataFolder;
		this.rosFolder = rosFolder;
		this.parent = manager;
	}
	
	@Override
	public void fileTransferRequest(FileTransferRequest request) {
		final IncomingFileTransfer transfer = request.accept();
		String fileToReceive = null;
	/*	fileToReceive = "./" + request.getFileName();
		try {
			transfer.receiveFile(new File(fileToReceive));
		} catch (SmackException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		while (!transfer.isDone()) {
			if (transfer.getStatus() == Status.refused) {
				System.out.println("Transfer refused");
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}*/
		System.out.println(" description in transfer() is: "+request.getDescription());
		// in tests, not need to store the configuration files
		try {
			// If it's the configuration from the Simulation Orchestrator
			if(request.getRequestor().compareTo(parent.getOrchestratorJID()) == 0) {
				final ChatManager chatmanager = ChatManager.getInstanceFor(parent.getConnection());
				final Chat newChat = chatmanager.chatWith(parent.getOrchestratorJID().asEntityBareJidIfPossible());
				if(StringUtils.isEmpty(dataFolder)|| StringUtils.isEmpty(rosFolder)) {
					String otherSimulationConfiguration = request.getDescription();  // Format is: SCID,visual:=false,....
					String[] simConfigs = otherSimulationConfiguration.split(",");
					this.parent.setSCID(simConfigs[0]);
					String parameters = "";
					for(int i=1; i<Arrays.asList(simConfigs).size(); i++) {
						parameters += simConfigs[i];
					}			
					this.parent.setSimulationConfiguration(parameters);	
					System.out.println("SimulationManager configured for optimization task: "+parent.getSCID());
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
		} catch (final SmackException | InterruptedException e) {
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
