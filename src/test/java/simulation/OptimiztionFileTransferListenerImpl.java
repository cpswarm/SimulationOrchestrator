package simulation;

import java.io.File;
import java.io.IOException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;

import com.google.gson.Gson;

import eu.cpswarm.optimization.messages.OptimizationToolConfiguredMessage;

public class OptimiztionFileTransferListenerImpl implements FileTransferListener {

	private DummyOptimizationTool parent = null;
	
	public OptimiztionFileTransferListenerImpl(DummyOptimizationTool parent) {
		this.parent = parent;
	}
	
	
	
	
	@Override
	public void fileTransferRequest(FileTransferRequest request) {
		final IncomingFileTransfer transfer = request.accept();
		String fileToReceive = null;
		// TODO save the state file to configure the OT for restarting optimization
		if(request.getRequestor().compareTo(parent.getOrchestratorJid()) == 0) {
			fileToReceive = "src/main/resources/stateTest.zip";
		}else {
			System.out.println("OT: Transfer refused");
			return;
		}

		try {
			transfer.receiveFile(new File(fileToReceive));

			while (!transfer.isDone()) {
				if (transfer.getStatus() == Status.refused) {
					System.out.println("Simulation Orchestrator: Transfer refused");
				}
				Thread.sleep(1000);
			}
			System.out.println("Simulation Orchestrator: state of the optimization tool saved for optimization "+request.getDescription());
		} catch (final SmackException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		// Since it is only for the test the file is not really stored but the one included in the resources is used
		System.out.println("Optimzation Tool: state of the optimization tool saved for optimization "+request.getDescription());
		OptimizationToolConfiguredMessage msg = new OptimizationToolConfiguredMessage(parent.getOptimizationID(), true);
		ChatManager chatManager = ChatManager.getInstanceFor(parent.getConnection());
		Chat chat = chatManager.chatWith(parent.getOrchestratorJid().asEntityBareJidIfPossible());
		Gson gson = new Gson();
		try {
			chat.send(gson.toJson(msg));
		} catch (NotConnectedException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
