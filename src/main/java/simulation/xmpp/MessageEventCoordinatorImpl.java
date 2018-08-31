package simulation.xmpp;

import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import eu.cpswarm.optimization.messages.MessageSerializer;
import eu.cpswarm.optimization.messages.OptimizationProgressMessage;
import eu.cpswarm.optimization.messages.OptimizationReplyMessage;
import eu.cpswarm.optimization.messages.OptimizationReplyMessage.Status;
import eu.cpswarm.optimization.messages.OptimizationStartedMessage;
import eu.cpswarm.optimization.messages.OptimizationCancelledMessage;
import simulation.GetProgressSender;
import simulation.SimulationOrchestrator;

/**
 *
 * The implementation of the receiver of messages
 *
 */
public final class MessageEventCoordinatorImpl implements IncomingChatMessageListener {

	private SimulationOrchestrator parent = null;
	private GetProgressSender getProgressSender = null;
	private Thread senderThread = null;
	
	public MessageEventCoordinatorImpl(final SimulationOrchestrator orchestrator) {
		this.parent = orchestrator;
	}
	
	@Override
	public void newIncomingMessage(EntityBareJid sender, Message msg, org.jivesoftware.smack.chat2.Chat chat) {
		// The message is sent from a manager
		if(sender.toString().startsWith("manager")) {
			if(!msg.getBody().equals("error")) {
				System.out.println("Received configuration ACK from "+sender.toString());
				parent.addManagerConfigured();
			}
		} else if(sender.compareTo(parent.getOptimizationJid().asBareJid())==0) {
			if(msg.getBody().contains("Progress")) {
				MessageSerializer serializer = new  MessageSerializer();
				OptimizationProgressMessage progress = serializer.fromJson(msg.getBody());
				System.out.println("Optimization "+progress.getId()+ ", progress:" + progress.getProgress() + "%");
				if(progress.getOperationStatus().equals(OptimizationReplyMessage.Status.OK)) {
					if(parent.isMonitoring()) {
						parent.getMqttClient().publish("/cpswarm/progress", serializer.toJson(progress).getBytes());
					}				
				}
			} else {
				MessageSerializer serializer = new MessageSerializer();
				System.out.println("Reply received: "+msg.getBody());
				if(msg.getBody().contains("OptimizationStarted")) {
					OptimizationStartedMessage reply = serializer.fromJson(msg.getBody());
					if(reply.getOperationStatus().equals(Status.OK) && reply.getId().equals(parent.getSimulationId())) {
						System.out.println("Transfering the configuration file to the manager");
						parent.transferFile(parent.getOptimizationJid().asEntityFullJidIfPossible(), parent.getConfigurationFile(), "configuration");
						getProgressSender = new GetProgressSender(parent);
						
						// create the thread
						senderThread = new Thread(getProgressSender);
	
						// run
						senderThread.start();
					}
				} else if(msg.getBody().contains("OptimizationStarted")) {
					OptimizationCancelledMessage reply = serializer.fromJson(msg.getBody());
					//TODO
				}
			}
		}
	}

}
