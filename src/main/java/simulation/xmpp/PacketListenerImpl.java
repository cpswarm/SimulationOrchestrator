package simulation.xmpp;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import messages.server.Server;
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
		// If the presence indicates that another user is trying to add the orchestrator
		// to its roster, it puts the presence in the queue of the ones to be handled
		if (presence.getType() == Presence.Type.subscribe) {
			parent.putSubscribeRequest(presence);
		} else {
			if(presence.isAvailable()) {
				Gson gson = new Gson();
				System.out.println(
						"presence received from " + presence.getFrom()+ ", status: "+presence.getStatus());
				try {
					if(presence.getFrom().toString().startsWith("manager")) {
						System.out.println("Adding Manager "+presence.getFrom().toString()+" to the list of the ones available");
						parent.putSimulationManager(JidCreate.entityBareFrom(presence.getFrom()), gson.fromJson(presence.getStatus(), Server.class));	
					} 	
				} catch (JsonSyntaxException | XmppStringprepException e) {
				}
			} else if(presence.getType().equals(Presence.Type.unavailable)){
				System.out.println(
						"presence received from " + presence.getFrom()+", type: "+presence.getType().toString());
				if(presence.getFrom()!=null && presence.getFrom().toString().startsWith("manager")) {
					System.out.println("Removing Manager "+presence.getFrom().toString()+" to the list of the ones available");
					try {
						parent.removeSimulationManager(JidCreate.entityBareFrom(presence.getFrom()));
					} catch (JsonSyntaxException | XmppStringprepException e) {
					}
				}/* else if(presence.getFrom().toString().startsWith("orchestrator") || presence.getFrom().toString().startsWith(parent.getOptimizationJid().toString())) {
						System.out.println("The connection is disconnected, reconnect");
						parent.reconnect();
				}*/
			}
		}
	}
}
