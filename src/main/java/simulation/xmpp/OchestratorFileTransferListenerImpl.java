package simulation.xmpp;

import java.io.File;
import java.io.IOException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;

import simulation.SimulationOrchestrator;

public class OchestratorFileTransferListenerImpl implements FileTransferListener {

	private String dataFolder = null;
		
	public OchestratorFileTransferListenerImpl(final String dataFolder) {
		this.dataFolder = dataFolder;
	}
	
	@Override
	public void fileTransferRequest(FileTransferRequest request) {
		final IncomingFileTransfer transfer = request.accept();
		String fileToReceive = null;
		// The configuration files are stored in the simulator folder, instead the candidate in the rosFolder
		if(request.getRequestor().toString().startsWith("manager")) {
			fileToReceive = dataFolder+request.getFileName();
		} else {
			System.out.println("Simulation Orchesrtator: Transfer refused");
		}
		try {
			transfer.receiveFile(new File(fileToReceive));

			while (!transfer.isDone()) {
				if (transfer.getStatus() == Status.refused) {
					System.out.println("Simulation Orchestrator: Transfer refused");
				}
				Thread.sleep(1000);
			}
			System.out.println("Simulation Orchestrator: status of the optimization tool saved for optimization "+request.getDescription());
		} catch (final SmackException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}	
}
