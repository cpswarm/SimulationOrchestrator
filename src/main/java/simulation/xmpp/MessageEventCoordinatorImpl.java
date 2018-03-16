package simulation.xmpp;

import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

/**
 *
 * The standard implementation of <code>MessageEventCoordinator</code>
 *
 */
public final class MessageEventCoordinatorImpl implements IncomingChatMessageListener {

	@Override
	public void newIncomingMessage(EntityBareJid arg0, Message arg1, org.jivesoftware.smack.chat2.Chat arg2) {
		// DO nothing		
	}

}
