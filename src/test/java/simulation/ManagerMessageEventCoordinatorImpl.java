package simulation;

import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import eu.cpswarm.optimization.messages.MessageSerializer;
import eu.cpswarm.optimization.messages.RunSimulationMessage;

/**
 *
 * The standard implementation of <code>MessageEventCoordinator</code>
 *
 */
public final class ManagerMessageEventCoordinatorImpl implements IncomingChatMessageListener {

	DummyManager parent = null;
	
	public ManagerMessageEventCoordinatorImpl(final DummyManager manager) {
		this.parent = manager;
	}
	
	@Override
	public void newIncomingMessage(EntityBareJid sender, Message msg, org.jivesoftware.smack.chat2.Chat chat) {
		if(sender.toString().startsWith("optimization")) {
			MessageSerializer serializer = new MessageSerializer();
			RunSimulationMessage runSimulation = serializer.fromJson(msg.getBody());
			System.out.println(msg.getBody());
			parent.setOptimizationID(runSimulation.getId());
			parent.setSimulationId(runSimulation.getSid());
			parent.setGuiEnabled(runSimulation.isGui());
			parent.setParams(runSimulation.getParams());
		}
	}

}
