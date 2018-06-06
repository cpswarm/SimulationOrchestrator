package simulation.xmpp;

import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import com.google.gson.Gson;

import messages.reply.OptimizationReply;
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
		} else if(sender.compareTo(parent.getOptimizationJid().asBareJid())==0) {
			Gson gson = new Gson();
			OptimizationReply reply = gson.fromJson(msg.getBody(), OptimizationReply.class);
			switch(reply.getTitle()) {
			case OptimizationReply.OPTIMIZATION_STARTED:
				if(reply.getOperationStatus().equals("OK") && reply.getID().equals(parent.getSimulationId())) {
					parent.transferFile(parent.getOptimizationJid().asEntityFullJidIfPossible(), parent.getConfigurationFile(), "configuration");
				}
				break;
			case OptimizationReply.OPTIMIZATION_CANCELLED:
				break;
			case OptimizationReply.OPTIMIZATION_PROGRESS:
				break;
			}
		}
	}

}
