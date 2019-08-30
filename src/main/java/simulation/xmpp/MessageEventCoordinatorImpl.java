package simulation.xmpp;

import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;
import eu.cpswarm.optimization.messages.MessageSerializer;
import eu.cpswarm.optimization.messages.SimulationResultMessage;
import eu.cpswarm.optimization.messages.SimulatorConfiguredMessage;
import eu.cpswarm.optimization.messages.OptimizationStatusMessage;
import eu.cpswarm.optimization.messages.OptimizationToolConfiguredMessage;
import simulation.SimulationOrchestrator;

/**
 *
 * The implementation of the receiver of messages
 *
 */
public final class MessageEventCoordinatorImpl implements IncomingChatMessageListener {
	private SimulationOrchestrator parent = null;
	private int counter = 0;
	
	public MessageEventCoordinatorImpl(final SimulationOrchestrator orchestrator) {
		this.parent = orchestrator;
		this.counter = orchestrator.getMAX_CONFIGURATION_ATTEMPTS();
	}
	
	@Override
	public void newIncomingMessage(EntityBareJid sender, Message msg, org.jivesoftware.smack.chat2.Chat chat) {
		// The message is sent from a manager
		MessageSerializer serializer = new MessageSerializer();
		if(msg.getBody().equals("error")) {
			System.out.println("error received from "+msg.getFrom());
		} else {
			eu.cpswarm.optimization.messages.Message message = serializer.fromJson(msg.getBody());
			// Check if it is a simple simulation or if it is an optimization
			// if the ID is the one set for the current optimization
			if (parent.getOptimizationId()==null 
					|| message.getOId().equals(parent.getOptimizationId())) {
				if (sender.toString().startsWith("manager")) {
					if (message instanceof SimulatorConfiguredMessage) {
						System.out.println("Received configuration ACK="
								+ ((SimulatorConfiguredMessage) message).getSuccess() + " from " + sender.toString());
						parent.handleACK(sender, ((SimulatorConfiguredMessage) message).getSuccess());
					} else if (message instanceof SimulationResultMessage) {
						if (((SimulationResultMessage) message).getSuccess()) {
							System.out.println("Received simulation result from " + sender.toString());
							parent.setSimulationDone(true);
						}
					}
				} else if (sender.compareTo(parent.getOptimizationJid().asBareJid()) == 0) {
					if (message instanceof OptimizationStatusMessage) {
						handleOptimizationStatusMessage((OptimizationStatusMessage) message, serializer);
					} else if (message instanceof OptimizationToolConfiguredMessage) {
						handleOptimizationToolConfiguredMessage((OptimizationToolConfiguredMessage) message);
					} else {
						System.out.println("Reply received: " + msg.getBody());
					}
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
		case ERROR_BAD_CONFIGURATION: /* it's a reply to the StartOptimizationMessage, because of the bad frevo configuration here we can not call to the handleOptimizationError() method, because no State file ever stored locally, SOO has to check the FrevoConfiguration */
			System.out.println("Optimization tool received a bad configuration");
			break;
		case ERROR_OPTIMIZAZION_FAILED: /* error occurs, optimization is not ongoing, but still online, it automatically reports the error status to SOO, then SOO needs to send back OptimizationState to OT for restarting */
			handleOptimizationError(optimizationStatus);
			break;
		default:
			break;
		}
	}

	private void handleOptimizationStarted(OptimizationStatusMessage reply) {
		parent.startGetOptimizationStateSender();
	}

	private void handleOptimizationRunningOrStopped(OptimizationStatusMessage reply) {
		if(reply.getOperationStatus().equals(OptimizationStatusMessage.Status.CANCELLED)) {
			parent.stopGetOptimizationStateSender();
			if(parent.getOptimizationId()!=null) {  // If COMPLETED or CANCELLED, OID=null
				parent.setOptimizationId(null);
			}
		}else {
			if(parent.isStateSenderSuspend()) {
				parent.restartGetOptimizationStateSender();  // when OT is online again, restart the state sender
			}
			System.out.println("Status of the current optimization: " + reply.getOId());
			System.out.println("Current progress: " + reply.getProgress() + "%");
			System.out.println("Current status: " + reply.getOperationStatus());
			System.out.println("Current best fitness value: " + reply.getBestFitnessValue());
			System.out.println("Current best candidate: " + reply.getBestController());
		}
	}
	
	private void handleOptimizationCompleted(OptimizationStatusMessage reply) {
		parent.stopGetOptimizationStateSender();
		System.out.println("Result of the current optimization: "+reply.getOId());
		System.out.println("Best fitness value: "+reply.getBestFitnessValue());
		System.out.println("Best candidate: "+reply.getBestController() );		
		parent.setSimulationDone(true);  // after a single simulation, SM will automatically send a SimulationResult=100 with success=true
		parent.setOptimizationId(null);
	}
	
	private void handleOptimizationError(OptimizationStatusMessage reply) {
		parent.stopGetOptimizationStateSender();
		// SOO try to reconfigure OT for maximum 3 times
		while(counter>0) {
			if(parent.sendOptimizationStateToOT()) {   /* state file transfered successfully-----if SOO wants to restart the optimization, just send OptimizationState to reconfigure the OT */
				break;
			} else {
				System.out.println("Error sending OptimizationState message");
				counter--;
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if(counter == 0) {
			System.out.println("Optimization tool can not be reconfigured any more!");
		}
	}
	
	
	private void handleOptimizationToolConfiguredMessage(OptimizationToolConfiguredMessage reply) {
		if (reply.getSuccess() != true) {
			// SOO try to reconfigure OT for maximum 3 times
			while (counter > 0) {
				if (parent.sendOptimizationStateToOT()) { /* state file transfered successfully, waiting for reply */
					break;
				} else {
					System.out.println("Error sending OptimizationState message");
					counter--;
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			if (counter == 0) {
				System.out.println("Optimization tool can not be reconfigured any more!");
			}
		} else {
			this.counter = parent.getMAX_CONFIGURATION_ATTEMPTS();
		    // use previous SMs with the same SCID
			parent.sendStartOptimization();
		}
	}

}
