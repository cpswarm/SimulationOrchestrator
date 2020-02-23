package simulation.xmpp;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import simulation.SimulationOrchestrator;

/**
 * Thread used to handle the subscription requests
 * @author co_da
 *
 */
public class SubscriptionHandler implements Runnable {
	private boolean run = true;
	private SimulationOrchestrator parent = null;
	
	public SubscriptionHandler(SimulationOrchestrator parent) {
		this.parent = parent;
	}
	
	@Override
	public void run() {
		while (run) {
			Presence presence = null;
			try {
				presence = parent.getSubscriptionRequest();
				SimulationOrchestrator.SEMAPHORE.acquire();
				System.out.println(
						"subscription request received from " + presence.getFrom());
				final Presence answerPresence = new Presence(
						Presence.Type.subscribe);
				answerPresence.setTo(presence.getFrom());
				parent.getConnection().sendStanza(answerPresence);
				final Roster roster = Roster.getInstanceFor(parent
						.getConnection());

				String entryType = "simulator";
				String descriptionToUse = "";
				RosterGroup group = null;
				group = roster.getGroup(entryType);
				if (!(group instanceof RosterGroup)) {
					group = roster.createGroup(entryType);
				}
				final String[] groups = { entryType };
				roster.createEntry(JidCreate.from(presence.getFrom()).asBareJid(), descriptionToUse, groups);
				System.out.println(
						"account " + presence.getFrom() + " subscribed");
				SimulationOrchestrator.SEMAPHORE.release();
			} catch (InterruptedException | NotConnectedException | NotLoggedInException | NoResponseException | XMPPErrorException | XmppStringprepException e) {
				System.out.println(
						"error adding the user: " + presence.getFrom());
				System.out.println("msg "+e.getMessage());
	            System.out.println("loc "+e.getLocalizedMessage());
	            System.out.println("cause "+e.getCause());
	            System.out.println("excep "+e);
				return;
			}
		}
	}

	public void stopThead() {
		run = false;
	}
}
