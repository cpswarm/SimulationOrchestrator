package simulation;

import java.util.UUID;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import com.google.gson.Gson;

import eu.cpswarm.optimization.messages.GetOptimizationStateMessage;
import eu.cpswarm.optimization.messages.GetOptimizationStatusMessage;
import eu.cpswarm.optimization.messages.MessageSerializer;
import eu.cpswarm.optimization.messages.OptimizationStatusMessage;
import eu.cpswarm.optimization.messages.OptimizationStatusMessage.Status;
import eu.cpswarm.optimization.messages.StartOptimizationMessage;
import eu.cpswarm.optimization.messages.RunSimulationMessage;
import eu.cpswarm.optimization.messages.SimulationResultMessage;


/**
 *
 * The standard implementation of <code>MessageEventCoordinator</code>
 *
 */
public final class OptimizationMessageEventCoordinatorImpl implements IncomingChatMessageListener {
	
	private int value = 0;
	private DummyOptimizationTool parent = null;
	private Boolean stopOptimzation = false; 
	
	public OptimizationMessageEventCoordinatorImpl(DummyOptimizationTool parent) {
		this.parent = parent;
	}
	
	@Override
	public void newIncomingMessage(EntityBareJid jid, Message msg, org.jivesoftware.smack.chat2.Chat chat) {
		Message message = new Message();
		MessageSerializer serializer = new MessageSerializer();
		eu.cpswarm.optimization.messages.Message msgReceived = serializer.fromJson(msg.getBody());
		// CHeck if the optimization ID has not been yet set (before the start optimization
		// or if the optimization ID is equal to the one set
		if(parent.getOptimizationID()==null
				|| parent.getOptimizationID().equals(msgReceived.getOId())) {
			if(msgReceived instanceof SimulationResultMessage) {
				SimulationResultMessage result = (SimulationResultMessage) msgReceived;
				// emergency_exit SCID is used to test simulations that conclude immediately
				if(this.stopOptimzation ||
						parent.getSCID().equals("emergency_exit")) {
					if(result.getFitnessValue()==100.0 && result.getSuccess()==true) {
						OptimizationStatusMessage message1 = new OptimizationStatusMessage(parent.getOptimizationID(), 1.0, Status.COMPLETED, 100.0, "bestController");
						Message msg1 = new Message();
						msg1.setBody(serializer.toJson(message1));
						ChatManager chatManager = org.jivesoftware.smack.chat2.ChatManager.getInstanceFor(parent.getConnection());
						try {
							Chat chatToUse = chatManager.chatWith(parent.getOrchestratorJid().asEntityBareJidIfPossible());
							chatToUse.send(msg1);
						} catch (NotConnectedException | InterruptedException e) {
							e.printStackTrace();
						}
					}
				// cpswarm_sar is used for optimizations that doesn't have to finish immediately because used to test the OT recovery  
				} else if(parent.getSCID().equals("cpswarm_sar")) {
					RunSimulationMessage runSimulation = new RunSimulationMessage(parent.getOptimizationID(), parent.getSCID(), "currentCandidate", "type");
					try {
						ChatManager chatManager = ChatManager.getInstanceFor(parent.getConnection());
						chat = chatManager.chatWith(msg.getFrom().asEntityBareJidIfPossible());
						Gson gson = new Gson();						
						chat.send(gson.toJson(runSimulation));
					} catch (NullPointerException | NotConnectedException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			} else if(msgReceived instanceof StartOptimizationMessage) {
				StartOptimizationMessage start = (StartOptimizationMessage) msgReceived; 
				parent.setOptimizationID(start.getOId());   /*---ADD-----Frevo's OID should be set when receiving the StartOptimization msg, not set in constructor */
				parent.setOptimizationConfiguration(start.getConfiguration());
				parent.setSCID(start.getSCID());
				System.out.println("OptimizationTool received StartOptimization: "+msg.getBody());
				OptimizationStatusMessage reply = new OptimizationStatusMessage(start.getOId(), 0.0, Status.STARTED, 0.0, null);  // default values
				String messageToSend = serializer.toJson(reply); 
				message.setBody(messageToSend);
				System.out.println("Sending reply to the StartOptimization: "+messageToSend);
				try {
					chat.send(message);
				} catch (NotConnectedException | InterruptedException e) {
					System.out.println("Error sending the reply");
					e.printStackTrace();
				}
				String simulationID = "";
				for(EntityFullJid manager : parent.getManagers()) {
					simulationID = UUID.randomUUID().toString();
					parent.setSimulationID(simulationID);
					RunSimulationMessage runSimulation = new RunSimulationMessage(parent.getOptimizationID(), simulationID, "currentCandidate", "type");
					ChatManager chatManager = ChatManager.getInstanceFor(parent.getConnection());
					chat = chatManager.chatWith(manager.asEntityBareJid());
					Gson gson = new Gson();
					try {
						chat.send(gson.toJson(runSimulation));
					} catch (NotConnectedException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			} else if(msgReceived instanceof GetOptimizationStatusMessage) {
				GetOptimizationStatusMessage getOptimizationStatus = (GetOptimizationStatusMessage) msgReceived;
				value +=10;
				System.out.println("OptimizationTool received GetOptimizationStatus: "+getOptimizationStatus.toString());
				Status status = null;
				if(parent.isOptimizationError()) {
					status = Status.ERROR_OPTIMIZAZION_FAILED;
				} else {
					status = Status.RUNNING;
				}
				OptimizationStatusMessage optimizationStatus = new OptimizationStatusMessage(parent.getOptimizationID(), 1.0, status, 80.0, "bestController");
				String messageToSend = serializer.toJson(optimizationStatus);
				message.setBody(messageToSend);
				System.out.println("OptimizationTool sending optimization staus "+messageToSend);
				try {
					chat.send(message);
				} catch (NotConnectedException | InterruptedException e) {
					System.out.println("Error sending the optimization status");
					e.printStackTrace();
				}
			} else if(msgReceived instanceof GetOptimizationStateMessage) {
				/* Not used because the file transfer doesn't work in case of test
				if(parent.sendOptimizationState()) {
					System.out.println("Error failing to report optimization state file to SOO");
				}
				*/
				return;
			} else {
				System.out.println("Reply received: " + msg.getBody());
			}

		}
	}
	
	/**
	 * This is the method used to indicate that the optimization ongoing has to be stopped or started again
	 * it is used to stop the optimization after that the Optimization Tool has returned online after have gone offline
	 * 
	 * @param stop - true to stop, false to start
	 */
	public void setStopOptimization(boolean stop) {
		this.stopOptimzation = stop;
		//It sends a run simulation message to restart the optimization if it has ben stopped
		RunSimulationMessage runSimulation = new RunSimulationMessage(parent.getOptimizationID(), parent.getSCID(), "currentCandidate", "type");
		try {
			ChatManager chatManager = ChatManager.getInstanceFor(parent.getConnection());
			Chat chat = chatManager.chatWith(JidCreate.entityBareFrom("manager_test@"+parent.getServerName()));
			Gson gson = new Gson();						
			chat.send(gson.toJson(runSimulation));
		} catch (NullPointerException | NotConnectedException | InterruptedException | XmppStringprepException e) {
			e.printStackTrace();
		}
	}
}
