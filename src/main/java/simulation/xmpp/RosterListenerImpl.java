package simulation.xmpp;

import java.util.Collection;
import java.util.StringTokenizer;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;
import org.jxmpp.jid.Jid;

import simulation.SimulationOrchestrator;

/**
 *
 * Implementation of the {@link RosterListener} class 
 *
 *
 */
public final class RosterListenerImpl implements RosterListener {

	private SimulationOrchestrator manager = null;

	/**
	 * Constructor of the roster listener
	 *
	 * @param xmppClient
	 *            client associated with the roster listener
	 *
	 */
	public RosterListenerImpl(final SimulationOrchestrator manager) {
		this.manager = manager;
	}


	@Override
	public void presenceChanged(final Presence presence) {
		try {
			if (!manager.getConnection().isConnected()) {
				return;
			}
			System.out.println(
					"SimulationOrchestrator, presence received: " + presence.getFrom()
							+ " " + presence);
			// Stores the bare JID without resource, because the roster
			// returns that info as user of a RosterEntry
			final StringTokenizer bareJID = new StringTokenizer(
					presence.getFrom().toString(), "/");
			final String jid = bareJID.nextToken();
			final Roster roster = Roster.getInstanceFor(manager
					.getConnection());
			// If the presence indicates that the bundle is available
			if (presence.getType() == Presence.Type.available) {
				handlePresenceAvailable(presence, jid);
			} else if (presence.getType() == Presence.Type.unavailable) {
				handlePrenceUnavailable(presence, jid, roster);
			}
			System.out.println(
					"SimulationOrchestrator, " + presence.getFrom() + " managed");
		} catch (final IllegalStateException e) {
			// The client is disconnected
			System.out.println(
					"SimulationOrchestrator, connection disconnected");
			return;
		} 
	}

	/**
	 * Handle a presence of type available
	 *
	 * @param presence
	 *            Presence received
	 * @param jid
	 *            JID that has sent the presence
	 *
	 * @throws AssertionError
	 *             if something wrong
	 */
	private void handlePresenceAvailable(final Presence presence,
			final String jid) {
		assert presence != null;
		assert jid != null;
		// If the bundle has gone away, it is removed from
		// the list of the available bundles
		if (presence.getMode() == Presence.Mode.away) {
			System.out.println(
					"SimulationOrchestrator, " + presence.getFrom() +"is offline");
			//TODO
			// handle launcher offline 
			
			// If instead it is an indication of available
			// it is inserted in the list of those available
		} else if ((presence.getMode() == Presence.Mode.available)
				|| (presence.getMode() == null)) {
			System.out.println(
					"SimulationOrchestrator, " + presence.getFrom() +"is online");
			//TODO
			// handle launcher online 
			
		}
	}

	/**
	 * Handle a presence of type available
	 *
	 * @param presence
	 *            Presence received
	 *
	 * @param jid
	 *            JID that has sent the presence
	 *
	 * @param roster
	 *            Roster of the bundle
	 *
	 * @throws AssertionError
	 *             if something wrong
	 */
	private void handlePrenceUnavailable(final Presence presence,
			final String jid, final Roster roster) {
		System.out.println(
				"SimulationOrchestrator, "+ presence.getFrom() + "is offline");
		//TODO
		// handle launcher offline
	}

	@Override
	public void entriesAdded(Collection<Jid> arg0) {
		System.out.println(
				"SimulationOrchestrator,  entry added in RosterListener, entry: " + arg0);
	}

	@Override
	public void entriesDeleted(Collection<Jid> arg0) {
		System.out.println(
				"SimulationOrchestrator, entry deleted in RosterListener, entry: " + arg0);
	}

	@Override
	public void entriesUpdated(Collection<Jid> arg0) {
		for (final Jid entry : arg0) {
			System.out.println(
					"SimulationOrchestrator, entry updated in RosterListener, entry: " + entry);
		}
	}
}