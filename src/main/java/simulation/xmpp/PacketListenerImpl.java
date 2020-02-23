package simulation.xmpp;

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import com.google.gson.JsonSyntaxException;

import eu.cpswarm.optimization.statuses.BaseStatus;
import eu.cpswarm.optimization.statuses.SimulationManagerStatus;
import eu.cpswarm.optimization.statuses.StatusSerializer;
import simulation.SimulationOrchestrator;


/**
 *
 * Packet listener of the presences received to request authorizations
 *
 */
public class PacketListenerImpl implements StanzaListener {

	private SimulationOrchestrator parent = null;

	/**
	 * Constructor of the listener of the presences received to request
	 * authorizations
	 *
	 * @param parent
	 *            {@link BundleManager} parent of this listener
	 *
	 *
	 * @throws AssertionError
	 *             if something is wrong
	 */
	public PacketListenerImpl(final SimulationOrchestrator parent) {
		assert parent != null;
		this.parent = parent;
	}

	@Override
	public void processStanza(final Stanza packet) {
		final Presence presence = (Presence) packet;
		try {
			// If the presence indicates that another user is trying to add the orchestrator
			// to its roster, it puts the presence in the queue of the ones to be handled
			if (presence.getType() == Presence.Type.subscribe) {

				SimulationOrchestrator.SEMAPHORE.acquire();
				parent.putSubscribeRequest(presence);
				SimulationOrchestrator.SEMAPHORE.release();
			} else {
				if (presence.isAvailable()) {
					System.out
					.println("presence received from " + presence.getFrom() + ", status: " + presence.getStatus());
					if (presence.getStatus() != null) {
						StatusSerializer serializer = new StatusSerializer();
						BaseStatus status = serializer.fromJson(presence.getStatus());
						switch (status.getType()) {
						case "OptimizationTool":
							if (parent.getOptimizationId() != null && presence.getFrom().compareTo(parent.getOptimizationJid()) == 0) {
								// if optimization was ever started(OID!=null), but OT was offline and online
								// again, SOO sends getOptimizationStatus in OT error handling workflow
								parent.sendGetOptimizationStatus();
							}
							break;
						case "SimulationManager":
							try {
								System.out.println("Adding Manager " + presence.getFrom().toString() + " to the list of the ones available");
								SimulationManagerStatus smStatus = (SimulationManagerStatus) status;
								if(smStatus.getSimulationConfigurationId() != null) {
									parent.putSimulationManager(JidCreate.entityBareFrom(presence.getFrom()), smStatus);
								}
							} catch (JsonSyntaxException | XmppStringprepException e) {
								e.printStackTrace();
							}
							break;
						}
					}
				} else if(presence.getType().equals(Presence.Type.unavailable)){
					System.out.println(
							"presence received from " + presence.getFrom()+", type: "+presence.getType().toString());
					if(presence.getFrom()!=null && presence.getFrom().toString().startsWith("manager")) {
						System.out.println("Removing Manager "+presence.getFrom().toString()+" from the list of the ones available");
						try {
							parent.removeSimulationManager(JidCreate.entityBareFrom(presence.getFrom()));
						} catch (JsonSyntaxException | XmppStringprepException e) {
						}
					} else if(parent.getOptimizationId()!=null && presence.getFrom().compareTo(parent.getOptimizationJid()) == 0) {
						System.out.println("The Optimization Tool is offline, stop to request the state");
						if(parent.isRecovery()) {
							parent.suspendGetOptimizationStateSender();
						}
					}
					/*	StatusSerializer serializer = new StatusSerializer();
				BaseStatus status = serializer.fromJson(presence.getStatus());
				switch(status.getType()) {
				case "OptimizationTool":
					if(parent.getOptimizationId()!=null && presence.getFrom().compareTo(parent.getOptimizationJid()) == 0) {
						System.out.println("The Optimization Tool is offline, stop to send the get status");
						if(parent.isRecovery()) {
							parent.suspendGetOptimizationStateSender();
						}
					}
				case "SimulationManager":
					System.out.println(
							"presence received from " + presence.getFrom()+", type: "+presence.getType().toString());
					if(presence.getFrom()!=null && presence.getFrom().toString().startsWith("manager")) {
						System.out.println("Removing Manager "+presence.getFrom().toString()+" to the list of the ones available");
						try {
							parent.removeSimulationManager(JidCreate.entityBareFrom(presence.getFrom()));
						} catch (JsonSyntaxException | XmppStringprepException e) {
						}
					}
				}*/
				}
			}
		} catch (InterruptedException e) {
			System.out.println("error adding the new presence in Queue : from " + presence.getFrom());
			e.printStackTrace();
		}
	}
}