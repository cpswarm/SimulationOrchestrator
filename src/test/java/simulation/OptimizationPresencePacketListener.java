package simulation;

import java.util.StringTokenizer;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jxmpp.jid.BareJid;

/**
 * Packet listener to be used in the {@link XMPPClient} to receive presences
 *
 */
public class OptimizationPresencePacketListener implements StanzaListener {
	private static final int TIME_TO_WAIT = 10000;

	private DummyOptimizationTool manager = null;

	private final Class<? extends ManagerMessageEventCoordinatorImpl> clazz;

	/**
	 * Constructor of the {@link OptimizationPresencePacketListener}
	 *
	 * @param xmppClient
	 *            {@link XMPPClient} that uses the packetListener
	 * @param clazz
	 *            class of the {@link MessageEventCoordinator} to use for the
	 *            chat
	 *
	 * @throws AssertionError
	 *             if something is wrong
	 */
	public OptimizationPresencePacketListener(final DummyOptimizationTool manager,
			final Class<? extends ManagerMessageEventCoordinatorImpl> clazz) {
		assert manager != null;
		assert clazz != null;
		this.manager = manager;
		this.clazz = clazz;
	}

	@Override
	public void processStanza(final Stanza packet) {
		final Presence presence = (Presence) packet;
		// Intercepts the presences that contain the request
		// of subscription to the roster
		if (presence.getType() == Presence.Type.subscribe) {
			try {
				final Roster roster = Roster.getInstanceFor(manager
						.getConnection());
				String description = "";
				final String entryType = "config";
				final RosterGroup group = getGroup(roster, entryType);
				final BareJid entryName = packet.getFrom().asBareJid();
				handleSubscriptionRequest(presence, roster, entryType,
							description, group, entryName);
			} catch (final XMPPException | MessageEventCoordinatorException e) {
				System.out.println(
						"Exception receieving the presence " + presence);
				System.out.println("cause "+e.getCause());
				System.out.println("excep "+e);
				e.printStackTrace();
			} catch (final IllegalStateException e) {
				// The client is disconnected
				System.out.println(
						"Connection disconnected, packet reception interrupted");
			}
		} else {
			try {
				if (!manager.getConnection().isConnected()) {
					return;
				}
				System.out.println(
						"SimulationManager "+manager.getJid()+", Presence received: " + presence.getFrom()
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
						"OptimizationTool "+manager.getJid()+"," + presence.getFrom() + " managed");
			} catch (final IllegalStateException e) {
				// The client is disconnected
				System.out.println(
						"OptimizationTool "+manager.getJid()+", connection disconnected");
				return;
			} 
		}
	}

	/**
	 * Method used to handle the subscription request
	 *
	 * @param presence
	 *            presence received
	 *
	 * @param roster
	 *            roster of the bundle
	 *
	 * @param entryType
	 *            type of the entry
	 *
	 * @param description
	 *            description of the bundle to be added
	 *
	 * @param group
	 *            group in which the entry has to be added in the roster
	 *
	 * @param entryName
	 *            name of the new entry
	 *
	 * @throws XMPPException
	 *             if there is an XMPP error
	 *
	 * @throws MessageEventCoordinatorException
	 *             if there is an error instantiating the message listener
	 *
	 * @throws AssertionError
	 *             if something is wrong
	 */
	private void handleSubscriptionRequest(final Presence presence,
			final Roster roster, final String entryType,
			final String description, final RosterGroup group,
			final BareJid entryName) throws XMPPException,
			MessageEventCoordinatorException {
		assert presence != null;
		assert roster != null;
		assert entryType != null;
		assert description != null;
		assert group != null;
		assert entryName != null;
		try {
			// Updates the roster, adding a client that has
			// requested the subscription
			synchronized (roster) {
				final RosterEntry entry = roster.getEntry(entryName);
				final String[] groups = { entryType };
				if (entry == null) {
					roster.createEntry(entryName, description, groups);
				} else if (!group.contains(entry)) {
					entry.setName(description);
					group.addEntry(entry);
				}
			}
			/*
			 * TODO
			 * understand if it is useful also in this case
			 
			// It waits some time to create the chat,
			// so the db manager can configure itself to
			// listen tp the chat creation
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					addChat(presence, entryType);
				}
			}, TIME_TO_WAIT);
			*/
			
			// Answer to the request of subscription,
			// sending its request
			final Presence answerPresence = new Presence(
					Presence.Type.subscribe);
			answerPresence.setTo(presence.getFrom());
			manager.getConnection().sendStanza(answerPresence);
		} catch (NotLoggedInException | NoResponseException
				| NotConnectedException | InterruptedException e) {
			System.out.println("Error receiving a subscription request.");
			System.out.println("cause "+e.getCause());
			System.out.println("excep "+e);
			e.printStackTrace();
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
					"Component "+manager.getJid()+"," + presence.getFrom() +" is offline");
			//TODO
			// handle orchestrator offline 
			
			// If instead it is an indication of available
			// it is inserted in the list of those available
		} else if ((presence.getMode() == Presence.Mode.available)
				|| (presence.getMode() == null)) {
			System.out.println(
					"Component "+manager.getJid()+"," + presence.getFrom() +" is online");
			//TODO
			// handle orchestrator online 
			
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
				"SimulationManager "+manager.getJid()+","+ presence.getFrom() + "is offline");
		//TODO
		// handle orchestrator offline
	}

	
	/**
	 * Method used to create a chat with the client that has sent the presence
	 * if it is a database manager for this type of data
	 *
	 * @param presence
	 *            {@link Presence} received
	 *
	 * @param entryType
	 *            Type of the entry
	 *
	 * @throws AssertionError
	 *             if something is wrong
	 *
	private void addChat(final Presence presence, final String entryType) {
		assert presence != null;
		assert entryType != null;
		if (!xmppClient.getChatFeatureManagersMap().containsKey(entryType)
				&& !xmppClient.getManagersList().isEmpty()
				&& xmppClient.getManagersList().contains(entryType)) {
			final ChatManager chatmanager = ChatManager
					.getInstanceFor(xmppClient.getConnection());
			MessageEventCoordinator msg = null;
			try {
				// Creates the chat and adds a listener
				// for the messages
				msg = xmppClient.getMessageEventCoordinatorFactory()
						.newMessageEventCoordinator(clazz);
			} catch (final MessageEventCoordinatorException e) {
				xmppClient.getLogger().error(
						"Error creating the message handler. Error: "
								+ GenericUtils.getErrorMessage(e), e);
			}
			Chat newChat = null;
			try {
				newChat = chatmanager.chatWith(JidCreate.entityBareFrom(presence.getFrom()));
				xmppClient.getChatFeatureManagersMap().put(entryType, newChat);
			} catch (XmppStringprepException e) {
				xmppClient.getLogger().error(
						"Error creating the message handler. Error: "
								+ GenericUtils.getErrorMessage(e), e);
			}
		}
	}
	*/

	/**
	 * Retrieves a group from the roster
	 *
	 * @param roster
	 *            roster to be used
	 *
	 * @param entryType
	 *            name of the group to be found
	 *
	 * @return the group (null if not present in the roster)
	 */
	private RosterGroup getGroup(final Roster roster, final String entryType) {
		RosterGroup group = null;
		group = roster.getGroup(entryType);
		if (!(group instanceof RosterGroup)) {
			group = roster.createGroup(entryType);
		}
		return group;
	}
}