package simulation;

import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import com.google.gson.Gson;

import messages.simulation.RunSimulation;

/**
 *
 * The standard implementation of <code>MessageEventCoordinator</code>
 *
 */
public final class ManagerMessageEventCoordinatorImpl implements IncomingChatMessageListener {

	DummyManager parent = null;
	
	public ManagerMessageEventCoordinatorImpl(final DummyManager manager) {
		this.parent = manager;
	}
	
	@Override
	public void newIncomingMessage(EntityBareJid sender, Message msg, org.jivesoftware.smack.chat2.Chat chat) {
		if(sender.toString().startsWith("optimization")) {
			Gson gson = new Gson();
			RunSimulation runSimulation = gson.fromJson(msg.getBody(), RunSimulation.class);
			System.out.println("Run simulation received "+runSimulation.getID()+ " gui enabled: "+runSimulation.getGui()+ " params: "+runSimulation.getParams());
			parent.setOptimizationID(runSimulation.getID());
			parent.setGuiEnabled(runSimulation.getGui());
			parent.setParams(runSimulation.getParams());
		}
	}

}
