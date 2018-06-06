package simulation;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jxmpp.jid.EntityBareJid;

import messages.start.StartOptimization;

public class OptimizationFileTransferListenerImpl implements FileTransferListener {

	private String dataFolder = null;
	private DummyOptimizationTool parent = null;
	private EntityBareJid orchestrator = null;
	
	public OptimizationFileTransferListenerImpl(final DummyOptimizationTool manager, final String dataFolder, final EntityBareJid orchestrator) {
		this.dataFolder = dataFolder;
		this.parent = manager;
		this.orchestrator = orchestrator;
	}
	
	@Override
	public void fileTransferRequest(FileTransferRequest request) {
		final IncomingFileTransfer transfer = request.accept();
		final String fileToReceive = dataFolder+request.getFileName();
		try {
			transfer.recieveFile(new File(fileToReceive));

			while (!transfer.isDone()) {
				if (transfer.getStatus() == Status.refused) {
					System.out.println("Transfer refused");
				}
				Thread.sleep(1000);
			}
			System.out.println("File received");
			Thread.sleep(1000);
		} catch (final SmackException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
