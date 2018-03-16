package simulation.xmpp;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

import simulation.SimulationOrchestrator;

/**
 *
 * listener of the events on the XMPP connection
 *
 */
public class ConnectionListenerImpl implements ConnectionListener {

	private SimulationOrchestrator parent;

	/**
	 * Constructor of the listener of the events on the XMPP connection
	 *
	 * @param parent
	 *            {@link BundleManager} parent of this listener
	 *
	 *
	 * @throws AssertionError
	 *             if something is wrong
	 */
	public ConnectionListenerImpl(final SimulationOrchestrator parent) {
		assert parent != null;
		this.parent = parent;
	}

	@Override
	public void connectionClosed() {
		System.out.println("The connection was closed normally.");
	}

	@Override
	public void connectionClosedOnError(final Exception arg0) {
		System.out.println(
				"The connection was closed due to an exception.");
	}

	@Override
	public void reconnectingIn(final int arg0) {
		System.out.println(
				"The connection will retry to reconnect in " + arg0
						+ " seconds.");
	}

	@Override
	public void reconnectionFailed(final Exception arg0) {
		System.out.println(
				"An attempt to connect to the server has failed.");

	}

	@Override
	public void reconnectionSuccessful() {
		System.out.println(
				"The connection has reconnected successfully to the server.");
	}

	@Override
	public void connected(final XMPPConnection arg0) {
		System.out.println(
				"The connection has connected successfully to the server.");
	}

	@Override
	public void authenticated(final XMPPConnection arg0, final boolean arg1) {
		System.out.println(
				"The connection has authenticated successfully to the server."
						+ ((arg1) ? " The connection has been resumed" : ""));
	}
}
