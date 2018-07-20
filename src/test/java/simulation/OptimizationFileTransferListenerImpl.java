package simulation;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.impl.JidCreate;

import messages.simulation.RunSimulation;
import messages.start.StartOptimization;

public class OptimizationFileTransferListenerImpl implements FileTransferListener {

	private String dataFolder = null;
	private DummyOptimizationTool parent = null;
	private EntityBareJid orchestrator = null;
	private EntityFullJid maanger = null;
	
	public OptimizationFileTransferListenerImpl(final DummyOptimizationTool optimizationTool, final String dataFolder, final EntityBareJid orchestrator, final EntityFullJid manager) {
		this.dataFolder = dataFolder;
		this.parent = optimizationTool;
		this.orchestrator = orchestrator;
		this.maanger = manager;
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
			System.out.println("Optimization Tool "+fileToReceive+" received");
			RunSimulation runSimulation = new RunSimulation();
			runSimulation.setID(parent.getSimulationID());
			runSimulation.setGui(parent.getGuiEnabled());
			runSimulation.setParams("");
			ChatManager chatManager = ChatManager.getInstanceFor(parent.getConnection());
			Chat chat = chatManager.chatWith(this.maanger.asEntityBareJid());
			chat.send(runSimulation.toString());
			// It transfers the candidate to the manager
			this.transferFile(this.maanger, this.dataFolder+"candidate.c", "candidate");
			Thread.sleep(1000);
		} catch (final SmackException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * This method verifies if the receiver supports the file transfer and in
	 * this case it sends a file
	 */
	public void transferFile(final EntityFullJid receiver, final String filePath, final String message) {
		final ServiceDiscoveryManager disco = ServiceDiscoveryManager
				.getInstanceFor(parent.getConnection());

		// Receives the info about the client of the receiver
		DiscoverInfo discoInfo = null;
		try {
			discoInfo = disco.discoverInfo(receiver);
		} catch (XMPPException | NoResponseException | NotConnectedException | InterruptedException e) {
			e.printStackTrace();
		}

		// Controls if the file transfer is supported
		if (discoInfo
				.containsFeature("http://jabber.org/protocol/si/profile/file-transfer")) {
			final FileTransferManager manager = FileTransferManager
					.getInstanceFor(parent.getConnection());
			OutgoingFileTransfer transfer = null;
			transfer = manager
					.createOutgoingFileTransfer(receiver);
			// Here the file is actually sent
			try {
				transfer.sendFile(new File(filePath), message);
				while (!transfer.isDone()) {
					if (transfer.getStatus() == Status.refused) {
						System.out.println("Transfer refused");
					}
					Thread.sleep(1000);
				}
			} catch (final SmackException | InterruptedException e) {
				e.printStackTrace();
			}
			final Status status = transfer.getStatus();
			if (status == Status.cancelled) {
				System.out.println("Transfer cancelled");
			} else if (status == Status.error) {
				System.out.println("Error in file transfer");
			} else if (status == Status.complete) {
				System.out.println("File transferred");
			}
		}
	}
}
