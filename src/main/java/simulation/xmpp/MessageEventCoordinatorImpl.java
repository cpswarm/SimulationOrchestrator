package simulation.xmpp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import eu.cpswarm.optimization.messages.MessageSerializer;
import eu.cpswarm.optimization.messages.OptimizationProgressMessage;
import eu.cpswarm.optimization.messages.ReplyMessage.Status;
import eu.cpswarm.optimization.messages.SimulationResultMessage;
import eu.cpswarm.optimization.messages.SimulatorConfiguredMessage;
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
		MessageSerializer serializer = new MessageSerializer();
		if(msg.getBody().equals("error")) {
			System.out.println("error received from "+msg.getFrom());
		} else {
			eu.cpswarm.optimization.messages.Message message = serializer.fromJson(msg.getBody());
			if(sender.toString().startsWith("manager")) {
				if(message instanceof SimulatorConfiguredMessage) {
					if(((SimulatorConfiguredMessage) message).getOperationStatus().equals(Status.OK)) {
						System.out.println("Received configuration ACK from "+sender.toString());
						parent.addManagerConfigured();	
					}
				} else if(message instanceof SimulationResultMessage) {
					if(((SimulationResultMessage) message).getOperationStatus().equals(Status.OK)) {
						System.out.println("Received simulation result from "+sender.toString());
						parent.setSimulationDone(true);	
					}
				}
			} else if(sender.compareTo(parent.getOptimizationJid().asBareJid())==0) {
				if(message instanceof OptimizationProgressMessage) {
					handleOptimizationProgressMessage((OptimizationProgressMessage) message, serializer);
				} else {
					System.out.println("Reply received: "+msg.getBody());
					if(message instanceof OptimizationCancelledMessage) {
						OptimizationCancelledMessage reply = serializer.fromJson(msg.getBody());
						//TODO
					}
				}
			}
		}
	}


	private void handleOptimizationProgressMessage(OptimizationProgressMessage progress, MessageSerializer serializer) {
		System.out.println("Optimization "+progress.getId()+ ", progress:" + progress.getProgress() + "%, fitness value: "+progress.getFitnessValue());
		if(progress.getOperationStatus().equals(ReplyMessage.Status.OK)) {
			if(progress.getProgress()==100.0) {
				if(senderThread!=null) {
					stopSenderThread();
				}
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				System.out.println("Final candidate: "+progress.getCandidate()+" received at "+SimulationOrchestrator.sdf.format(timestamp));
				parent.setSimulationDone(true);
				// The final candidate contains the optimized values for the parameters
				// and has to be saved in the output folder of the launcher to be used as result
				// to be passed to the deployment tool
			    BufferedWriter writer;
				try {
					writer = new BufferedWriter(new FileWriter(parent.getOutputDataFolder()+"parameters.yaml"));
				    writer.write(progress.getCandidate());
				    writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		// There is an error in the optimization, which is stopped
		} else {
			if(senderThread!=null) {
				stopSenderThread();
			}
			parent.evaluateSimulationManagers();
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
