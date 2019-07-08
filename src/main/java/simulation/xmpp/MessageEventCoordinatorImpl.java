package simulation.xmpp;

import java.sql.Timestamp;

import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import eu.cpswarm.optimization.messages.MessageSerializer;
import eu.cpswarm.optimization.messages.SimulationResultMessage;
import eu.cpswarm.optimization.messages.SimulatorConfiguredMessage;

import eu.cpswarm.optimization.messages.OptimizationStatusMessage;
import eu.cpswarm.optimization.messages.OptimizationStatusMessage.Status;
import eu.cpswarm.optimization.messages.ReplyMessage;
import simulation.GetOptimizationStatusSender;
import simulation.SimulationOrchestrator;

/**
 *
 * The implementation of the receiver of messages
 *
 */
public final class MessageEventCoordinatorImpl implements IncomingChatMessageListener {

	private SimulationOrchestrator parent = null;
	private GetOptimizationStatusSender getOptimizationStatusSender = null;
	private Thread senderThread = null;
	private boolean monitoring;
	
	public MessageEventCoordinatorImpl(final SimulationOrchestrator orchestrator) {
		this.parent = orchestrator;
		this.monitoring = parent.getMonitoring();
	}
	
	@Override
	public void newIncomingMessage(EntityBareJid sender, Message msg, org.jivesoftware.smack.chat2.Chat chat) {
		// The message is sent from a manager
		MessageSerializer serializer = new MessageSerializer();
		if(msg.getBody().equals("error")) {
			System.out.println("error received from "+msg.getFrom());
		} else {
			eu.cpswarm.optimization.messages.Message message = serializer.fromJson(msg.getBody());
			if(sender.toString().startsWith("manager")) {
				if(message instanceof SimulatorConfiguredMessage) {
					if(((SimulatorConfiguredMessage) message).getSuccess()) {
						System.out.println("Received configuration ACK from "+sender.toString());
						parent.addManagerConfigured();	
					}
				} else if(message instanceof SimulationResultMessage) {
					if(((SimulationResultMessage) message).getSuccess()) {
						System.out.println("Received simulation result from "+sender.toString());
						parent.setSimulationDone(true);	
					}
				}
			} else if(sender.compareTo(parent.getOptimizationJid().asBareJid())==0) {
				if(message instanceof OptimizationStatusMessage) {
					handleOptimizationStatusMessage((OptimizationStatusMessage) message, serializer);
				} else {
					System.out.println("Reply received: "+msg.getBody());
				}
			}
		}
	}
	
	private void handleOptimizationStatusMessage(OptimizationStatusMessage optimizationStatus, MessageSerializer serializer) {
		switch(optimizationStatus.getOperationStatus()) {
		case STARTED:
			handleOptimizationStarted(optimizationStatus);
			break;
		case RUNNING:
		case CANCELLED:
			handleOptimizationRunningOrStopped(optimizationStatus);
			break;
		case COMPLETED:
			handleOptimizationCompleted(optimizationStatus);
			break;
		case ERROR_BAD_CONFIGURATION:
		case ERROR_OPTIMIZAZION_FAILED:
			handleOptimizationError(optimizationStatus);
			break;
		default:
			break;
		}
	}

	private void handleOptimizationStarted(OptimizationStatusMessage reply) {
		if(reply.getOId().equals(parent.getOptimizationId()) && parent.isRecovery()) {
			getOptimizationStatusSender = new GetOptimizationStatusSender(parent);
			
			// create the thread
			senderThread = new Thread(getOptimizationStatusSender);

			// run
			senderThread.start();
		}
	}

	private void handleOptimizationRunningOrStopped(OptimizationStatusMessage reply) {
		if(reply.getOId().equals(parent.getOptimizationId())) {
			System.out.println("Status of the current optimization: "+reply.getOId());
			System.out.println("Current progress: "+reply.getProgress()+"%");
			System.out.println("Current status: "+reply.getOperationStatus());
			System.out.println("Current best fitness value: "+reply.getBestFitnessValue());
			System.out.println("Current best candidate: "+reply.getBestController() );
		}
	}
	
	private void handleOptimizationCompleted(OptimizationStatusMessage reply) {
		if(senderThread!=null) {
			stopSenderThread();
		}
		System.out.println("Result of the current optimization: "+reply.getOId());
		System.out.println("Best fitness value: "+reply.getBestFitnessValue());
		System.out.println("Best candidate: "+reply.getBestController() );		
		parent.setSimulationDone(true);
	}
	
	private void handleOptimizationError(OptimizationStatusMessage reply) {
		if(senderThread!=null) {
			stopSenderThread();
		}
		parent.evaluateSimulationManagers();
	}
	
	private void stopSenderThread() {
		getOptimizationStatusSender.setCanRun(false);
		try {
			senderThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		senderThread = null;
		getOptimizationStatusSender = null;
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
