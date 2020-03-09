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
		MessageSerializer serializer = new MessageSerializer();
		RunSimulationMessage runSimulation = serializer.fromJson(msg.getBody());
		if(sender.toString().startsWith("optimization")) {
			System.out.println("SimulationManager received "+msg.getBody());
			parent.setOptimizationID(runSimulation.getOptimizationId());
			parent.setSimulationId(runSimulation.getSimulationId());
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			parent.publishFitness(80.0);
		} else if(sender.toString().startsWith("orchestrator")) {   /* for single sim, OID = null */
			if (parent.isOrchestratorAvailable()) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				SimulationResultMessage message = new SimulationResultMessage(parent.getOptimizationId(), true, runSimulation.getSimulationId(), 100);
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
}
