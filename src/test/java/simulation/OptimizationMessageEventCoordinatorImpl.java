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
	
	public OptimizationMessageEventCoordinatorImpl(DummyOptimizationTool parent) {
		this.parent = parent;
	}
	
	@Override
	public void newIncomingMessage(EntityBareJid jid, Message msg, org.jivesoftware.smack.chat2.Chat chat) {
		Message message = new Message();
		MessageSerializer serializer = new MessageSerializer();
		eu.cpswarm.optimization.messages.Message msgReceived = serializer.fromJson(msg.getBody());
	if(msgReceived instanceof SimulationResultMessage) {
		SimulationResultMessage result = (SimulationResultMessage) msgReceived; 
		if(result.getFitnessValue()==100.0 && result.getSuccess()==true) {
			OptimizationStatusMessage message1 = new OptimizationStatusMessage(parent.getOptimizationID(), 50.9, Status.COMPLETED, 2.0, "test");
			Message msg1 = new Message();
			msg1.setBody(serializer.toJson(message1));
			ChatManager chatManager = org.jivesoftware.smack.chat2.ChatManager.getInstanceFor(parent.getConnection());
			try {
				Chat chatToUse = chatManager.chatWith(JidCreate.entityBareFrom("orchestrator@"+parent.getServerName()));
				chatToUse.send(msg1);
			} catch (NotConnectedException | InterruptedException | XmppStringprepException e) {
				e.printStackTrace();
			}
		}
	} else if(msgReceived instanceof StartOptimizationMessage) {
			StartOptimizationMessage start = (StartOptimizationMessage) msgReceived; 
			parent.setOptimizationID(start.getOId());   /*---ADD-----Frevo's OID should be set when receiving the StartOptimization msg, not set in constructor */
			parent.setOptimizationConfiguration(start.getConfiguration());
			System.out.println("OptimizationTool received StartOptimization: "+msg.getBody());
			OptimizationStatusMessage reply = new OptimizationStatusMessage(start.getOId(), 50.9, Status.STARTED, 2.0, "test");
			String messageToSend = serializer.toJson(reply); 
			message.setBody(messageToSend);
			System.out.println("Sending reply to the StartOptimization: "+messageToSend);
			try {
				chat.send(message);
			} catch (NotConnectedException | InterruptedException e) {
				System.out.println("Error sending the reply");
				e.printStackTrace();
			}
			String simulationID = UUID.randomUUID().toString();
			parent.setSimulationID(simulationID);
			for(EntityFullJid manager : parent.getManagers()) {
			//	RunSimulationMessage runSimulation = new RunSimulationMessage(parent.getOptimizationID(), "Run Simulation", simulationID, "");
				RunSimulationMessage runSimulation = new RunSimulationMessage(parent.getOptimizationID(), simulationID, "Candidate", "type");
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
			System.out.println("OptimizationTool received GetOptimizationStatus: "+msg.getBody());
			OptimizationStatusMessage status = new OptimizationStatusMessage(getOptimizationStatus.getOId(), 100, Status.COMPLETED, 2.0, "test");
			String messageToSend = serializer.toJson(status);
			message.setBody(messageToSend);
			System.out.println("OptimizationTool sending optimization staus "+messageToSend);
			try {
				chat.send(message);
			} catch (NotConnectedException | InterruptedException e) {
				System.out.println("Error sending the optimization status");
				e.printStackTrace();
			}
		}
		
	}
}
