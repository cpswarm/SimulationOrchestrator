package simulation.xmpp;

import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import eu.cpswarm.optimization.messages.MessageSerializer;
import eu.cpswarm.optimization.messages.OptimizationProgressMessage;
import eu.cpswarm.optimization.messages.ReplyMessage.Status;
import eu.cpswarm.optimization.messages.OptimizationStartedMessage;
import eu.cpswarm.optimization.messages.ReplyMessage;
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
				System.out.println("Optimization "+progress.getId()+ ", progress:" + progress.getProgress() + "%, fitness value: "+progress.getFitnessValue());
				if(progress.getOperationStatus().equals(ReplyMessage.Status.OK)) {
					if(progress.getProgress()==100.0) {
						if(senderThread!=null) {
							stopSenderThread();
						}
						System.out.println("Final candidate: "+progress.getCandidate());
					}
					if(parent.isMonitoring()) {
						parent.getMqttClient().publish("/cpswarm/progress", serializer.toJson(progress).getBytes());
					}
				// There is an error in the optimzation, which is stopped
				} else {
					if(senderThread!=null) {
						stopSenderThread();
					}
					parent.evaluateSimulationManagers();
				}
			} else {
				MessageSerializer serializer = new MessageSerializer();
				System.out.println("Reply received: "+msg.getBody());
				if(msg.getBody().contains("OptimizationStarted")) {
					OptimizationStartedMessage reply = serializer.fromJson(msg.getBody());
					if(reply.getOperationStatus().equals(Status.OK) && reply.getId().equals(parent.getOptimizationId()) && parent.getMonitoring().booleanValue()) {
						getProgressSender = new GetProgressSender(parent);
						
						// create the thread
						senderThread = new Thread(getProgressSender);
	
						// run
						senderThread.start();
					}
				} else if(msg.getBody().contains("OptimizationCancelled")) {
					OptimizationCancelledMessage reply = serializer.fromJson(msg.getBody());
					//TODO
				}
			}
		}
	}

	private void stopSenderThread() {
		getProgressSender.setCanRun(false);
		try {
			senderThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		senderThread = null;
		getProgressSender = null;
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
