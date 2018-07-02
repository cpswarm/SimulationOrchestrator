package simulation;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import com.google.gson.Gson;

import messages.progress.GetProgress;
import messages.reply.OptimizationReply;
import messages.start.StartOptimization;

/**
 *
 * The standard implementation of <code>MessageEventCoordinator</code>
 *
 */
public final class OptimizationMessageEventCoordinatorImpl implements IncomingChatMessageListener {
	
	private int value = 0;
	
	@Override
	public void newIncomingMessage(EntityBareJid jid, Message msg, org.jivesoftware.smack.chat2.Chat chat) {
		OptimizationReply reply = new OptimizationReply();
		Gson gson = new Gson();
		if(msg.getBody().contains("Start")) {
			StartOptimization start = gson.fromJson(msg.getBody(), StartOptimization.class);
			reply.setID(start.getID());
			reply.setTitle(OptimizationReply.OPTIMIZATION_STARTED);
			reply.setOperationStatus("OK");
		} else if(msg.getBody().contains("Get")) {
			GetProgress getProgress = gson.fromJson(msg.getBody(), GetProgress.class);
			value +=10;
			reply.setID(getProgress.getID());
			reply.setTitle(OptimizationReply.OPTIMIZATION_PROGRESS);
			reply.setOperationStatus(value + "%");
			
		}
		Message message = new Message();
		message.setBody(gson.toJson(reply));
		try {
			chat.send(message);
		} catch (NotConnectedException | InterruptedException e) {
			System.out.println("Error sending the reply");
			e.printStackTrace();
		}
	}
}
