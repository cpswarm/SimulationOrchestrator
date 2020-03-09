package simulation;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Presence;
import org.jxmpp.jid.EntityFullJid;

import com.google.gson.Gson;

import eu.cpswarm.optimization.messages.OptimizationStatusMessage;
import eu.cpswarm.optimization.messages.RunSimulationMessage;
import eu.cpswarm.optimization.messages.StartOptimizationMessage;
import eu.cpswarm.optimization.parameters.Parameter;
import eu.cpswarm.optimization.parameters.ParameterOptimizationConfiguration;
import eu.cpswarm.optimization.statuses.OptimizationStatusType;
import eu.cpswarm.optimization.statuses.OptimizationTaskStatus;
import eu.cpswarm.optimization.statuses.OptimizationToolStatus;

public class OptimizationRunSimulationSender implements Runnable{

	private OptimizationMessageEventCoordinatorImpl messageListener = null;
	private DummyOptimizationTool parent = null;
	private StartOptimizationMessage start = null;
	private List<Parameter> parameters = null;
	private boolean canRun = true;
	
	public OptimizationRunSimulationSender(final DummyOptimizationTool parent, final StartOptimizationMessage start) {
		this.parent = parent;
		messageListener = parent.getMessageListener();
		this.start = start;
		parameters = messageListener.getParameters();
	}
	
	@Override
	public void run() {
		org.jivesoftware.smack.chat2.Chat chat = null;
		Gson gson = new Gson();
		ParameterOptimizationConfiguration optConfig = start.getConfiguration();
		parent.setOptimizationID(start.getOptimizationId());
		parent.setOptimizationConfiguration(optConfig);
		parent.setSCID(start.getSimulationConfigurationId());
		System.out.println("\nOptimizationTool received StartOptimization: " + gson.toJson(start));
		if (optConfig.getGeneration() == 0) {
			final Presence presence = new Presence(Presence.Type.available);
			OptimizationToolStatus status = new OptimizationToolStatus(new ArrayList<OptimizationTaskStatus>());
			presence.setStatus(gson.toJson(status)); // firstly send a presence without any optimization task
			try {
				parent.getConnection().sendStanza(presence);
			} catch (final NotConnectedException | InterruptedException e) {
				e.printStackTrace();
			}
			status = null;
			parent.sendPresence(OptimizationStatusType.STARTED);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else if (optConfig.getGeneration() > 0 && optConfig.getGeneration() < parent.getMaxGeneration()) {
			parent.sendPresence(OptimizationStatusType.RUNNING);
		}

		for (int i = 0; i < ((parent.getMaxGeneration() + 1) * parent.getCandidateCount()* parent.getVariantCount()); i++) {
			String simulationID = "";
			int sid = 0;
			if (parent.getSimulationID() != null) {
				sid = new Integer(parent.getSimulationID()).intValue();
			}
			i = sid;
			if (parent.getManagers().size() > 0) {
				for (EntityFullJid manager : parent.getManagers()) {
					while (parent.isOffline()) {
						System.out.println("OT is offline, waiting..........\n");
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					simulationID = String.valueOf(sid);
					if (sid < ((parent.getMaxGeneration() + 1) * parent.getCandidateCount()
							* parent.getVariantCount())) {
						RunSimulationMessage runSimulation = new RunSimulationMessage(parent.getOptimizationID(),
								simulationID, Long.valueOf(1234).longValue(), parameters);
						ChatManager chatManager = ChatManager.getInstanceFor(parent.getConnection());
						chat = chatManager.chatWith(manager.asEntityBareJid());
						try {
							// if (parent.getConnection().isConnected()) {
							chat.send(gson.toJson(runSimulation));
							/*
							 * } else { System.out.println("\nlost connection.........."); do { try {
							 * Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }
							 * } while(!parent.getConnection().isConnected()); sid -= 1; }
							 */
						} catch (NotConnectedException | InterruptedException e) {
							e.printStackTrace();
							sid -= 1;
						}
						for (int counter = 0; counter < 4; counter++) {
							if (messageListener.getResultID() != sid) {
								try {
									Thread.sleep(2000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							} else {
								break;
							}
						}
						if (sid == (parent.getCandidateCount() * parent.getVariantCount())) {
							parent.sendPresence(OptimizationStatusType.RUNNING);
						} else if (sid > (parent.getCandidateCount() * parent.getVariantCount() * 2 - 1)
								&& sid % (parent.getCandidateCount() * parent.getVariantCount()) == 0) {
							parent.setGeneration(parent.getGeneration() + 1);
							parent.sendPresence(OptimizationStatusType.RUNNING);
						}
						if (sid == 3 && parent.getOptimizationError() != null) {
							parent.disconnect();
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							parent.reconnect();
						}
						sid += 1; // SID increases by 1 each time
						parent.setSimulationID(String.valueOf(sid));
						i = sid - 1;
					} else {
						break;
					}
				}
				continue;
			}
			if (!canRun)
				return;
		}
		parent.setGeneration(parent.getGeneration() + 1);
		parent.sendPresence(OptimizationStatusType.COMPLETE);
	}
	
	public void setCanRun(boolean canRun) {
		this.canRun = canRun;
	}

}


