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
	private DummyManager parent = null;
	private EntityBareJid orchestrator = null;
	
	public ManagerFileTransferListenerImpl(final DummyManager manager, final String dataFolder, final EntityBareJid orchestrator) {
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
			final ChatManager chatmanager = ChatManager.getInstanceFor(parent.getConnection());
			final Chat newChat = chatmanager.chatWith(orchestrator);
			if(unzipFiles(fileToReceive)) {
				newChat.send("simulator configured");
			} else {
				newChat.send("error");
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
				File newFile = new File(dataFolder + fileName);
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
