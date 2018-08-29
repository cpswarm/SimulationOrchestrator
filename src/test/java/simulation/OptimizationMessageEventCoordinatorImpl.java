package simulation;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import eu.cpswarm.optimization.messages.GetProgressMessage;
import eu.cpswarm.optimization.messages.MessageSerializer;
import eu.cpswarm.optimization.messages.OptimizationProgressMessage;
import eu.cpswarm.optimization.messages.OptimizationReplyMessage;
import eu.cpswarm.optimization.messages.StartOptimizationMessage;


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
		Message message = new Message();
		MessageSerializer serializer = new MessageSerializer();
		if(msg.getBody().contains("Start")) {
			StartOptimizationMessage start = serializer.fromJson(msg.getBody());
			parent.setGuiEnabled(start.isGui());
			parent.setManagers(start.getSimulationManagers());
			System.out.println("OptimizationTool received StartOptimization: "+msg.getBody());
			OptimizationReplyMessage reply = new OptimizationReplyMessage();
			reply.setId(start.getId());
			reply.setOperationStatus("OK");
			message.setBody(serializer.toString(reply));
			System.out.println("Sending reply to the StartOptimization: "+serializer.toString(reply));
			try {
				chat.send(message);
			} catch (NotConnectedException | InterruptedException e) {
				System.out.println("Error sending the reply");
				e.printStackTrace();
			}
		} else if(msg.getBody().contains("Get")) {
			GetProgressMessage getProgress = serializer.fromJson(msg.getBody());
			value +=10;
			System.out.println("OptimizationTool received GetProgress: "+msg.getBody());
			OptimizationProgressMessage progress = new OptimizationProgressMessage();
			progress.setId(getProgress.getId());
			progress.setOperationStatus(String.valueOf(value));
			progress.setUom("%");
			String messageToSend = serializer.toString(progress);
			message.setBody(messageToSend);
			System.out.println("OptimizationTool sending progress "+messageToSend);
			try {
				chat.send(message);
			} catch (NotConnectedException | InterruptedException e) {
				System.out.println("Error sending the progress");
				e.printStackTrace();
			}
		}
		
	}
}
