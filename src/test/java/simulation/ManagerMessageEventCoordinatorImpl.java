package simulation;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import eu.cpswarm.optimization.messages.MessageSerializer;
import eu.cpswarm.optimization.messages.RunSimulationMessage;
import eu.cpswarm.optimization.messages.SimulationResultMessage;

/**
 *
 * The standard implementation of <code>MessageEventCoordinator</code>
 *
 */
public final class ManagerMessageEventCoordinatorImpl implements IncomingChatMessageListener {
	private DummyManager parent = null;
	
	public ManagerMessageEventCoordinatorImpl(final DummyManager manager) {
		this.parent = manager;
	}
	
	@Override
	public void newIncomingMessage(EntityBareJid sender, Message msg, org.jivesoftware.smack.chat2.Chat chat) {
		if(sender.toString().startsWith("optimization")) {
			MessageSerializer serializer = new MessageSerializer();
			RunSimulationMessage runSimulation = serializer.fromJson(msg.getBody());
			System.out.println("SimulationManager received "+msg.getBody());
			parent.setOptimizationID(runSimulation.getOId());
			parent.setSimulationId(runSimulation.getSid());
			if(parent.isOptimizationToolAvailable()) {
				parent.publishFitness(100.0);
			}
		} else if(sender.toString().startsWith("orchestrator")) {
			SimulationResultMessage message = new SimulationResultMessage(parent.getOptimizationId(), true, "", 100);
			MessageSerializer serializer = new MessageSerializer();
			Message messageToSend = new Message();
			messageToSend.setBody(serializer.toJson(message));
			try {
				chat.send(messageToSend);
			} catch (NotConnectedException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
