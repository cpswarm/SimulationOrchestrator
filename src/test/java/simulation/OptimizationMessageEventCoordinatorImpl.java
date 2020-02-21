package simulation;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

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
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import eu.cpswarm.optimization.messages.GetOptimizationStatusMessage;
import eu.cpswarm.optimization.messages.MessageSerializer;
import eu.cpswarm.optimization.messages.OptimizationStatusMessage;
import eu.cpswarm.optimization.messages.StartOptimizationMessage;
import eu.cpswarm.optimization.parameters.Parameter;
import eu.cpswarm.optimization.statuses.OptimizationStatusType;
import eu.cpswarm.optimization.messages.RunSimulationMessage;
import eu.cpswarm.optimization.messages.SimulationResultMessage;


/**
 *
 * The standard implementation of <code>MessageEventCoordinator</code>
 *
 */
public final class OptimizationMessageEventCoordinatorImpl implements IncomingChatMessageListener {
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
		Gson gson = new Gson();
		JsonReader reader = null;
		try {
			reader = new JsonReader(new FileReader("src/main/resources/candidate.json"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		List<Parameter> parameters = gson.fromJson(reader, new TypeToken<List<Parameter>>(){}.getType()); 
		// CHeck if the optimization ID has not been yet set (before the start optimization 
		// or if the optimization ID is equal to the one set
		if(parent.getOptimizationID()==null   // before receiving StartOptimization, OID = null
					|| (parent.getOptimizationID()!=null && parent.getOptimizationID().equals(msgReceived.getOptimizationId()))) {
			if(msgReceived instanceof SimulationResultMessage) {
				SimulationResultMessage result = (SimulationResultMessage) msgReceived;
				// emergency_exit SCID is used to test simulations that conclude immediately
				if(this.stopOptimzation ||
						parent.getSCID().equals("emergency_exit")) {
					if(result.getFitnessValue()==100.0 && result.getSuccess()==true) {
						OptimizationStatusMessage message1 = new OptimizationStatusMessage(parent.getOptimizationID(), OptimizationStatusType.COMPLETE, 100.0, parent.getOptimizationConfiguration().getMaximumGeneration(), parent.getOptimizationConfiguration().getMaximumGeneration(), parameters, parent.getOptimizationConfiguration());
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
			//		String newSimulationID = UUID.randomUUID().toString();
					int newSID = new Integer(parent.getSimulationID()).intValue()+1;
					String newSimulationID = String.valueOf(newSID);
					parent.setSimulationID(newSimulationID);
					RunSimulationMessage runSimulation = new RunSimulationMessage(parent.getOptimizationID(), newSimulationID, Long.valueOf(1234).longValue(), parameters);
					try {
						ChatManager chatManager = ChatManager.getInstanceFor(parent.getConnection());
						chat = chatManager.chatWith(msg.getFrom().asEntityBareJidIfPossible());						
						chat.send(gson.toJson(runSimulation));
					} catch (NullPointerException | NotConnectedException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			} else if(msgReceived instanceof StartOptimizationMessage) {
				StartOptimizationMessage start = (StartOptimizationMessage) msgReceived; 
				parent.setOptimizationID(start.getOptimizationId());   /*---ADD-----Frevo's OID should be set when receiving the StartOptimization msg, not set in constructor */
				parent.setOptimizationConfiguration(start.getConfiguration());
				parent.setSCID(start.getSimulationConfigurationId());
				System.out.println("OptimizationTool received StartOptimization: "+msg.getBody());
				OptimizationStatusMessage reply = new OptimizationStatusMessage(start.getOptimizationId(), OptimizationStatusType.STARTED, 0.0, start.getConfiguration().getGeneration(),start.getConfiguration().getMaximumGeneration(), new ArrayList<Parameter>(), start.getConfiguration());  // default values
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
				int sid = 0;
				if(parent.getSimulationID()!=null) {
					sid = new Integer(parent.getSimulationID()).intValue();
				}
				for(EntityFullJid manager : parent.getManagers()) {
					while(parent.isOffline()) {
						System.out.println("OT is offline, waiting..........\n");
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					sid += 1;  // SID increases by 1 each time
					simulationID = String.valueOf(sid);
					parent.setSimulationID(simulationID);
					RunSimulationMessage runSimulation = new RunSimulationMessage(parent.getOptimizationID(), simulationID, Long.valueOf(1234).longValue(), parameters);
					ChatManager chatManager = ChatManager.getInstanceFor(parent.getConnection());
					chat = chatManager.chatWith(manager.asEntityBareJid());
					try {
						chat.send(gson.toJson(runSimulation));
					} catch (NotConnectedException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			} else if(msgReceived instanceof GetOptimizationStatusMessage) {
				GetOptimizationStatusMessage getOptimizationStatus = (GetOptimizationStatusMessage) msgReceived;
				System.out.println("OptimizationTool received GetOptimizationStatus: "+serializer.toJson(getOptimizationStatus));
				OptimizationStatusType status = null;
				if(parent.isOptimizationError()) {
					status = OptimizationStatusType.ERROR;
				} else {
					status = OptimizationStatusType.RUNNING;
				}
				OptimizationStatusMessage optimizationStatus = new OptimizationStatusMessage(parent.getOptimizationID(), status, 80.0, 2, parent.getOptimizationConfiguration().getMaximumGeneration(), parameters, parent.getOptimizationConfiguration());
				String messageToSend = serializer.toJson(optimizationStatus);
				message.setBody(messageToSend);
				System.out.println("OptimizationTool sending optimization staus "+messageToSend);
				try {
					chat.send(message);
				} catch (NotConnectedException | InterruptedException e) {
					System.out.println("Error sending the optimization status");
					e.printStackTrace();
				}
			}  else {
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
		System.out.println("\n set the last stop candidate\n");
		//It sends a run simulation message to restart the optimization if it has been stopped
		int newSID = new Integer(parent.getSimulationID()).intValue()+1;
		String newSimulationID = String.valueOf(newSID);
		Gson gson = new Gson();
		JsonReader reader = null;
		try {
			reader = new JsonReader(new FileReader("src/main/resources/candidate.json"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		List<Parameter> parameters = gson.fromJson(reader, new TypeToken<List<Parameter>>(){}.getType()); 
		RunSimulationMessage runSimulation = new RunSimulationMessage(parent.getOptimizationID(), newSimulationID, Long.valueOf(1234).longValue(), parameters);
		try {
			ChatManager chatManager = ChatManager.getInstanceFor(parent.getConnection());
			Chat chat = chatManager.chatWith(JidCreate.entityBareFrom("manager_bamboo@"+parent.getServerName()));				
			chat.send(gson.toJson(runSimulation));
		} catch (NullPointerException | NotConnectedException | InterruptedException | XmppStringprepException e) {
			e.printStackTrace();
		}
	}
}
