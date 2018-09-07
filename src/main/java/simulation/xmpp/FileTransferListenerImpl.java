package simulation.xmpp;

import java.io.File;
import java.io.IOException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

import simulation.SimulationOrchestrator;

import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;


public abstract class FileTransferListenerImpl implements FileTransferListener {

	protected SimulationOrchestrator parent = null;
	protected String dataFolder = null;
	
	public FileTransferListenerImpl(final SimulationOrchestrator orchestrator) {
		this.parent = orchestrator;
	}
	
	@Override
	public void fileTransferRequest(FileTransferRequest request) {
		final IncomingFileTransfer transfer = request.accept();
		final String fileToReceive = dataFolder+request.getFileName();
		try {
			transfer.receiveFile(new File(fileToReceive));

			while (!transfer.isDone()) {
				if (transfer.getStatus() == Status.refused) {
					System.out.println("Transfer refused");
				}
				Thread.sleep(1000);
			}
			System.out.println("File received");
			Thread.sleep(1000);
			//TODO Confirm the optimization results
		} catch (final SmackException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
