package simulation.xmpp;

/**
 *
 * Implementation of an exception due to a not correct creation of the listeners
 * for the chat messages
 *
 */
public final class MessageEventCoordinatorException extends Exception {

	private static final long serialVersionUID = 2135516640380616721L;

	/**
	 * Constructor of the <code>MessageEventException</code>
	 */
	public MessageEventCoordinatorException() {
		super();
	}

	/**
	 * Constructor with a message
	 *
	 * @param message
	 *            message to set
	 */
	public MessageEventCoordinatorException(final String message) {
		super(message);
	}

	/**
	 * Constructor with a message and a parent exception
	 *
	 * @param message
	 *            message to set
	 * 
	 * @param e
	 *            parent exception
	 */
	public MessageEventCoordinatorException(final String message,
			final Throwable e) {
		super(message, e);
	}
}
