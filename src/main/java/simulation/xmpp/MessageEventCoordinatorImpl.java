package simulation.xmpp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;
import com.google.gson.Gson;

import eu.cpswarm.optimization.messages.MessageSerializer;
import eu.cpswarm.optimization.messages.SimulationResultMessage;
import eu.cpswarm.optimization.messages.SimulatorConfiguredMessage;
import eu.cpswarm.optimization.parameters.Parameter;
import eu.cpswarm.optimization.statuses.OptimizationStatusType;
import eu.cpswarm.optimization.messages.OptimizationStatusMessage;
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
		System.out.println("RECEIVED msg from ......."+msg.getFrom() +" msg= "+msg.getBody());
		MessageSerializer serializer = new MessageSerializer();
		if(msg.getBody().equals("error")) {
			System.out.println("error received from "+msg.getFrom());
		} else {
			eu.cpswarm.optimization.messages.Message message = serializer.fromJson(msg.getBody());
			// Check if it is a simple simulation or if it is an optimization
			// if the ID is the one set for the current optimization
		//	if (parent.getOptimizationId()==null || message.getOptimizationId()==null) {
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
					System.out.println("status= "+((OptimizationStatusMessage) message).getStatusType());
					handleOptimizationStatusMessage((OptimizationStatusMessage) message, serializer);
				} else {
					System.out.println("Reply received: " + msg.getBody());
				}
			}
		//	}
			
		}
	}
	
	private void handleOptimizationStatusMessage(OptimizationStatusMessage optimizationStatus, MessageSerializer serializer) {
		switch(optimizationStatus.getStatusType()) {
		case STARTED:
			handleOptimizationStarted(optimizationStatus);
			break;
		case RUNNING:
		case CANCELLED:
			handleOptimizationRunningOrStopped(optimizationStatus);
			break;
		case COMPLETE:
			handleOptimizationCompleted(optimizationStatus);
			break;
		case ERROR: /* error occurs, optimization is not ongoing, but still online, it automatically reports the error status to SOO, then SOO needs to send back OptimizationState to OT for restarting */
			handleOptimizationError(optimizationStatus);
			break;
		default:
			break;
		}
	}

	private void handleOptimizationStarted(OptimizationStatusMessage reply) {
		System.out.println(" received started msg..... ");
	//	parent.startGetOptimizationStateSender();
	}

	private void handleOptimizationRunningOrStopped(OptimizationStatusMessage reply) {
		if(reply.getStatusType().equals(OptimizationStatusType.CANCELLED)) {
			parent.stopGetOptimizationStateSender();
			if(parent.getOptimizationId()!=null) {
				parent.setOptimizationId(null);
			}
		}else {
			if(parent.isStateSenderSuspend()) {
				parent.restartGetOptimizationStateSender();  // when OT is online again, restart the state sender
			}
			System.out.println("Status of the current optimization: " + reply.getOptimizationId());
			System.out.println("Current status: " + reply.getStatusType());
			System.out.println("Current best fitness value: " + reply.getBestFitness());
			Gson gson = new Gson();
			System.out.println("Current best candidate: " + gson.toJson(reply.getBestParameters()));
			System.out.println("Current configuration: " + reply.getConfiguration().toString());
			parent.setConfiguration(reply.getConfiguration());
		}
	}
	
	private void handleOptimizationCompleted(OptimizationStatusMessage reply) {
		parent.stopGetOptimizationStateSender();
		Gson gson = new Gson();
		String parameters = gson.toJson(reply.getBestParameters());
		System.out.println("Optimization "+reply.getOptimizationId()+ " completed with the best fitness value: "+reply.getBestFitness());
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println("Final candidate: "+parameters+" received at "+SimulationOrchestrator.sdf.format(timestamp));
		// The final candidate contains the optimized values for the parameters
		// and has to be saved in the output folder of the launcher to be used as result
		// to be passed to the deployment tool
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(parent.getOutputDataFolder()+"candidate.json"));
			
			writer.write(parameters);
			writer.close();
			Map<String, Parameter> params = new HashMap<String,Parameter>();
			for (Parameter param : reply.getBestParameters()) {
				if(param.getMeta().startsWith("file")) {
					params.put(param.getName(),param);
				}
			}
			if(!params.isEmpty()) {
				// Check the name of the file where to save values 
				// currently only one file per time is supported
				// the values must be saved in one only file
				List<Parameter> list = new ArrayList<Parameter>(params.values());
				StringTokenizer tokens = new StringTokenizer(list.get(0).getMeta(),":");
				tokens.nextToken();
				String fileName = tokens.nextToken();
				// Save the file
				saveFile(params, parent.getOutputDataFolder()+fileName+".yaml");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
		parent.setSimulationDone(true);  // after a single simulation, SM will automatically send a SimulationResult=100 with success=true
		parent.setOptimizationId(null);
	}
	
	private void handleOptimizationError(OptimizationStatusMessage reply) {
		System.out.println("Handling Optimization tool Error");
		parent.stopGetOptimizationStateSender();
		parent.setConfiguration(reply.getConfiguration());
		// SOO try to reconfigure OT for maximum 3 times
		while(counter>0) {
			if(parent.sendStartOptimization()) {  // directly reply with the frevoConfiguration sent back through OptimizationStatusMessage
				counter = parent.getMAX_CONFIGURATION_ATTEMPTS();
				break;
			} else {
				System.out.println("Error sending StartOptimization message");
				counter--;
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if(counter == 0) {
			System.out.println("Optimization tool can not be reconfigured any more! Exit Optimization");
		}
	}
	
	private boolean saveFile(final Map<String, Parameter> params, final String parameterFilePath) {
		List<Parameter> paramsToAdd  = new ArrayList<Parameter>(params.values());
		File parameterFile = new File(parameterFilePath);
		List<String> fileContent = null;
		try {
			if (parameterFile.exists()) {
				fileContent = new ArrayList<>(
						Files.readAllLines(Paths.get(parameterFilePath), StandardCharsets.UTF_8)); 
				for (int j = 0; j < fileContent.size(); j++) {
					StringTokenizer tokens = new StringTokenizer(fileContent.get(j).trim(),":");
					String paramName =  tokens.nextToken();
					if (params.containsKey(paramName)) {
						fileContent.set(j, paramName + ": " + params.get(paramName).getValue());
						paramsToAdd.remove(params.get(paramName));
					}
				}
			} else {
				fileContent = new ArrayList<String>();
				parameterFile.createNewFile();
			}
			for(Parameter param : paramsToAdd) {
				fileContent.add(param.getName()+": "+ param.getValue());
			}
			Files.write(Paths.get(parameterFilePath), fileContent, StandardCharsets.UTF_8);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
