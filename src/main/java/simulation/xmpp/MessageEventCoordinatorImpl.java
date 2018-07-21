package simulation.xmpp;

import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import com.google.gson.Gson;

import messages.progress.OptimizationProgress;
import messages.reply.OptimizationReply;
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
				System.out.print("Received configuration ACK from "+sender.toString());
				parent.addManagerConfigured();
			}
		} else if(sender.compareTo(parent.getOptimizationJid().asBareJid())==0) {
			Gson gson = new Gson();
			if(msg.getBody().contains("Progress")) {
				OptimizationProgress progress = gson.fromJson(msg.getBody(), OptimizationProgress.class);
				System.out.println("Optimization "+progress.getID()+ ", progress:" + progress.getOperationStatus() + " " +progress.getUom());
				if(parent.isMonitoring()) {
					parent.getMqttClient().publish("/cpswarm/progress", gson.toJson(progress).getBytes());
				}				
			} else {
				OptimizationReply reply = gson.fromJson(msg.getBody(), OptimizationReply.class);
				switch(reply.getTitle()) {
				case OptimizationReply.OPTIMIZATION_STARTED:
					if(reply.getOperationStatus().equals("OK") && reply.getID().equals(parent.getSimulationId())) {
						parent.transferFile(parent.getOptimizationJid().asEntityFullJidIfPossible(), parent.getConfigurationFile(), "configuration");
						getProgressSender = new GetProgressSender(parent);
						
						// create the thread
						senderThread = new Thread(getProgressSender);
	
						// run
						senderThread.start();
					}
					break;
				case OptimizationReply.OPTIMIZATION_CANCELLED:
					break;
				}
			}
		}
	}

}
