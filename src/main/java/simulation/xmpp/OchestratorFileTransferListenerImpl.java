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

	private SimulationOrchestrator parent = null;
	private String outputDataFolder = null;
		
	public OchestratorFileTransferListenerImpl(final SimulationOrchestrator orchestrator, final String outputDataFolder) {
		this.parent = orchestrator;
		this.outputDataFolder = outputDataFolder;
	}
	
	@Override
	public void fileTransferRequest(FileTransferRequest request) {
		final IncomingFileTransfer transfer = request.accept();
		String fileToReceive = null;
		// // the state file called SCID from OT will saved in the subfolder named with OID in the outputDataFolder, instead refuse the request
		if(request.getRequestor().compareTo(parent.getOptimizationJid()) == 0) {
			if(SimulationOrchestrator.TEST) {
				fileToReceive = "src/main/resources/stateTest.zip";
			} else {
				fileToReceive = outputDataFolder+parent.getOptimizationId()+File.separator+request.getFileName();
			}
		} else {
			System.out.println("Simulation orchestrator: Transfer refused");
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
	}	
}
