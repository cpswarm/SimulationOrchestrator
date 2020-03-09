package simulation;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
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
import eu.cpswarm.optimization.parameters.ParameterOptimizationConfiguration;
import eu.cpswarm.optimization.statuses.OptimizationStatusType;
import eu.cpswarm.optimization.statuses.OptimizationTaskStatus;
import eu.cpswarm.optimization.statuses.OptimizationToolStatus;
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
	private int resultID = 1;
	private List<Parameter> parameters = null;
	private OptimizationRunSimulationSender sender = null;
	private Thread runSimulationSenderThread = null;
	
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
			e1.printStackTrace();
		}
		parameters = gson.fromJson(reader, new TypeToken<List<Parameter>>(){}.getType()); 
		// CHeck if the optimization ID has not been yet set (before the start optimization 
		// or if the optimization ID is equal to the one set
		if(parent.getOptimizationID()==null   // before receiving StartOptimization, OID = null
					|| (parent.getOptimizationID()!=null && parent.getOptimizationID().equals(msgReceived.getOptimizationId()))) {
			if(msgReceived instanceof SimulationResultMessage) {
				SimulationResultMessage result = (SimulationResultMessage) msgReceived;
				resultID = new Integer(result.getSimulationId()).intValue();
				
			} else if(msgReceived instanceof StartOptimizationMessage) {
				StartOptimizationMessage start = (StartOptimizationMessage) msgReceived; 
				if(runSimulationSenderThread != null) {
					this.sender.setCanRun(false);
					try {
					//	Thread.sleep(5000);
						runSimulationSenderThread.join();
						sender = null;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				this.sender = new OptimizationRunSimulationSender(parent, start);
				runSimulationSenderThread = new Thread(sender);
				runSimulationSenderThread.start();
				
			} else if(msgReceived instanceof GetOptimizationStatusMessage) {
				GetOptimizationStatusMessage getOptimizationStatus = (GetOptimizationStatusMessage) msgReceived;
				System.out.println("\nOptimizationTool received GetOptimizationStatus for "+getOptimizationStatus.getOptimizationId());
				if(!parent.getTasksList().get(0).getOptimizationId().equals(getOptimizationStatus.getOptimizationId())) {
					System.out.println("\nNo any existing optimization task");
					return;
				}
				OptimizationStatusType status = null;
				if(parent.getGeneration() == parent.getMaxGeneration()) {
					status = OptimizationStatusType.COMPLETE;
				} else if(parent.getTasksList().get(0).getStatusType().equals(OptimizationStatusType.ERROR)) {
					System.out.println("\n opt error...................................."  );
					status = OptimizationStatusType.ERROR;
				} else {
					if(parent.getTasksList().get(0).getStatusType().equals(OptimizationStatusType.RUNNING)) {
						status = OptimizationStatusType.RUNNING;
					}else {
						status = OptimizationStatusType.STARTED;
					}
				}
				OptimizationStatusMessage optimizationStatus = null;
				if(status.equals(OptimizationStatusType.COMPLETE))
					optimizationStatus = new OptimizationStatusMessage(parent.getOptimizationID(), status, 98.0, parent.getGeneration(), parent.getMaxGeneration(), parameters, parent.getOptimizationConfiguration());
				else
					optimizationStatus = new OptimizationStatusMessage(parent.getOptimizationID(), status, 80.0, parent.getGeneration(), parent.getMaxGeneration(), parameters, parent.getOptimizationConfiguration());
				String messageToSend = serializer.toJson(optimizationStatus);
				message.setBody(messageToSend);
				System.out.println("OptimizationTool sending optimizationStaus "+messageToSend);
				try {
					chat.send(message);
				} catch (NotConnectedException | InterruptedException e) {
					System.out.println("Error sending the optimization status");
					e.printStackTrace();
				}
			}  else {
				System.out.println("OptimizationTool received message: " + msg.getBody());
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
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//It sends a run simulation message to restart the optimization if it has been stopped
		int newSID = new Integer(parent.getSimulationID()).intValue()+1;
		String newSimulationID = String.valueOf(newSID);
		Gson gson = new Gson();
		JsonReader reader = null;
		try {
			reader = new JsonReader(new FileReader("src/main/resources/candidate.json"));
		} catch (FileNotFoundException e1) {
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
	
	public int getResultID() {
		return resultID;
	}
	
	public List<Parameter> getParameters(){
		return parameters;
	}
}
