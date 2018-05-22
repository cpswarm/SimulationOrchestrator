package simulation.xmpp;

import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import simulation.SimulationOrchestrator;

/**
 *
 * The implementation of the receiver of messages
 *
 */
public final class MessageEventCoordinatorImpl implements IncomingChatMessageListener {

	private SimulationOrchestrator parent = null;
	
	public MessageEventCoordinatorImpl(final SimulationOrchestrator orchestrator) {
		this.parent = orchestrator;
	}
	
	@Override
	public void newIncomingMessage(EntityBareJid sender, Message msg, org.jivesoftware.smack.chat2.Chat chat) {
		// The message is sent from a manager
		if(sender.toString().startsWith("manager")) {
			if(!msg.getBody().equals("error")) {
				parent.addManagerConfigured();
			}
		} else if(sender.compareTo(parent.getOptimizationJid())==0) {
			
		}
	}

}
