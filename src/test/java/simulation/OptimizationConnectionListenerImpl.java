package simulation;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

/**
 *
 * Implementation of the listener of the connection status
 *
 */
public final class OptimizationConnectionListenerImpl implements ConnectionListener {

	private final DummyOptimizationTool parent;

	/**
	 * Constructor of the listener of the connection
	 *
	 * @param parent
	 *            <code>XMPPClient</code> instance
	 *
	 *
	 * @throws AssertionError
	 *             if something is wrong
	 */
	public OptimizationConnectionListenerImpl(final DummyOptimizationTool parent) {
		assert parent != null;
		this.parent = parent;
	}

	@Override
	public void connectionClosed() {
		System.out.println(
				"XMPPClient The connection was closed normally.");
		// TODO
		// handle disconnection
	}

	@Override
	public void connectionClosedOnError(final Exception arg0) {
		System.out.println(
				"XMPPClient the connection was closed due to an exception.");
		// TODO
		// handle disconnection
	}

	@Override
	public void reconnectingIn(final int arg0) {
		System.out.println(
				"XMPPClient the connection will retry to reconnect in " + arg0
						+ " seconds.");
	}

	@Override
	public void reconnectionFailed(final Exception arg0) {
		System.out.println(
				"XMPPClient an attempt to connect to the server has failed.");
	}

	@Override
	public void reconnectionSuccessful() {
		System.out.println(
				"The connection has reconnected successfully to the server.");
		// TODO
		// handle reconnection
	}

	@Override
	public void connected(final XMPPConnection arg0) {
		System.out.println(
				"The connection has connected successfully to the server.");
	}

	@Override
	public void authenticated(final XMPPConnection paramXMPPConnection,
			final boolean paramBoolean) {
		System.out.println(
				"The connection has authenticated successfully to the server."
						+ ((paramBoolean) ? " The connection has been resumed"
								: ""));
	}
}
