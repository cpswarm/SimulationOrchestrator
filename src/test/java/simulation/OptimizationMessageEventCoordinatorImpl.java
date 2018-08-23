package simulation;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import com.google.gson.Gson;

import messages.progress.GetProgress;
import messages.progress.OptimizationProgress;
import messages.reply.OptimizationReply;
import messages.start.StartOptimization;

/**
 *
 * The standard implementation of <code>MessageEventCoordinator</code>
 *
 */
public final class OptimizationMessageEventCoordinatorImpl implements IncomingChatMessageListener {
	
	private int value = 0;
	private DummyOptimizationTool parent = null;
	
	public OptimizationMessageEventCoordinatorImpl(DummyOptimizationTool parent) {
		this.parent = parent;
	}
	
	@Override
	public void newIncomingMessage(EntityBareJid jid, Message msg, org.jivesoftware.smack.chat2.Chat chat) {
		Gson gson = new Gson();
		Message message = new Message();
		if(msg.getBody().contains("Start")) {
			StartOptimization start = gson.fromJson(msg.getBody(), StartOptimization.class);
			parent.setGuiEnabled(start.getGui());
			parent.setManagers(start.getSimulationManagers());
			System.out.println("OptimizationTool received StartOptimization: "+msg.getBody());
			OptimizationReply reply = new OptimizationReply();
			reply.setID(start.getID());
			reply.setTitle(OptimizationReply.OPTIMIZATION_STARTED);
			reply.setOperationStatus("OK");
			message.setBody(gson.toJson(reply));
			System.out.println("Sending reply to the StartOptimization: "+gson.toJson(reply));
			try {
				chat.send(message);
			} catch (NotConnectedException | InterruptedException e) {
				System.out.println("Error sending the reply");
				e.printStackTrace();
			}
		} else if(msg.getBody().contains("Get")) {
			GetProgress getProgress = gson.fromJson(msg.getBody(), GetProgress.class);
			value +=10;
			OptimizationProgress progress = new OptimizationProgress();
			progress.setID(getProgress.getID());
			progress.setOperationStatus(String.valueOf(value));
			progress.setUom("%");
			message.setBody(gson.toJson(progress));
			try {
				chat.send(message);
			} catch (NotConnectedException | InterruptedException e) {
				System.out.println("Error sending the progress");
				e.printStackTrace();
			}
		}
		
	}
}
