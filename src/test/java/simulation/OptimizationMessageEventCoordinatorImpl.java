package simulation;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import com.google.gson.Gson;

import messages.reply.OptimizationReply;
import messages.start.StartOptimization;

/**
 *
 * The standard implementation of <code>MessageEventCoordinator</code>
 *
 */
public final class OptimizationMessageEventCoordinatorImpl implements IncomingChatMessageListener {
	
	@Override
	public void newIncomingMessage(EntityBareJid jid, Message msg, org.jivesoftware.smack.chat2.Chat chat) {
		Gson gson = new Gson();
		StartOptimization start = gson.fromJson(msg.getBody(), StartOptimization.class);
		OptimizationReply reply = new OptimizationReply();
		reply.setID(start.getID());
		reply.setTitle(OptimizationReply.OPTIMIZATION_STARTED);
		reply.setOperationStatus("OK");
		Message message = new Message();
		message.setBody(gson.toJson(reply));
		try {
			chat.send(message);
		} catch (NotConnectedException | InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("Error sending the reply to StartOptimzation");
			e.printStackTrace();
		}
		
	}

}
