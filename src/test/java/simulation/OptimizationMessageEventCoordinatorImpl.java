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

import eu.cpswarm.optimization.messages.GetProgressMessage;
import eu.cpswarm.optimization.messages.MessageSerializer;
import eu.cpswarm.optimization.messages.OptimizationProgressMessage;
import eu.cpswarm.optimization.messages.StartOptimizationMessage;
import eu.cpswarm.optimization.messages.ReplyMessage.Status;
import eu.cpswarm.optimization.messages.RunSimulationMessage;
import eu.cpswarm.optimization.messages.SimulationResultMessage;
import eu.cpswarm.optimization.messages.OptimizationStartedMessage;
import eu.cpswarm.optimization.messages.Parameter;
import eu.cpswarm.optimization.messages.ParameterSet;


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
		ParameterSet parameters = new ParameterSet();
		Parameter parameter = new Parameter("test", "test", 2);
		parameters.getParameters().add(parameter);
		if(msgReceived instanceof SimulationResultMessage) {
			SimulationResultMessage result = (SimulationResultMessage) msgReceived; 
			if(result.getFitnessValue()==100.0) {
				OptimizationProgressMessage message1 = new OptimizationProgressMessage(parent.getOptimizationID(), "final result", Status.OK, 100.0, 100.0, parameters);
				Message msg1 = new Message();
				msg1.setBody(serializer.toJson(message1));
				ChatManager chatManager = org.jivesoftware.smack.chat2.ChatManager.getInstanceFor(parent.getConnection());
				try {
					Chat chatToUse = chatManager.chatWith(JidCreate.entityBareFrom("orchestrator_bamboo@"+parent.getServerName()));
					chatToUse.send(msg1);
				} catch (NotConnectedException | InterruptedException | XmppStringprepException e) {
					e.printStackTrace();
				}
			}
		} else if(msgReceived instanceof StartOptimizationMessage) {
			StartOptimizationMessage start = (StartOptimizationMessage) msgReceived; 
			parent.setOptimizationConfiguration(start.getOptimizationConfiguration());
			parent.setSimulationConfiguration(start.getSimulationConfiguration());
			parent.setManagers(start.getSimulationManagers());
			System.out.println("OptimizationTool received StartOptimization: "+msg.getBody());
			OptimizationStartedMessage reply = new OptimizationStartedMessage(start.getId(), "Optimization started", Status.OK);
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
				RunSimulationMessage runSimulation = new RunSimulationMessage(parent.getOptimizationID(), "Run Simulation", simulationID, parent.getSimulationConfiguration(), parameters);
				ChatManager chatManager = ChatManager.getInstanceFor(parent.getConnection());
				chat = chatManager.chatWith(manager.asEntityBareJid());
				Gson gson = new Gson();
				try {
					chat.send(gson.toJson(runSimulation));
				} catch (NotConnectedException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else if(msgReceived instanceof GetProgressMessage) {
			GetProgressMessage getProgress = (GetProgressMessage) msgReceived;
			value +=10;
			System.out.println("OptimizationTool received GetProgress: "+msg.getBody());
			OptimizationProgressMessage progress = new OptimizationProgressMessage(getProgress.getId(), "Optimzation progress", Status.OK, value, -1-24, parameters);
			String messageToSend = serializer.toJson(progress);
			message.setBody(messageToSend);
			System.out.println("OptimizationTool sending progress "+messageToSend);
			try {
				chat.send(message);
			} catch (NotConnectedException | InterruptedException e) {
				System.out.println("Error sending the progress");
				e.printStackTrace();
			}
		}
	}
}
