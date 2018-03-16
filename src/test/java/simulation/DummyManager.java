package simulation;

import messages.control.Control;

public class DummyManager {

	private boolean controlReceived = false;
	
	public DummyManager(String serverIP, String serverName, String serverPassword) {
		
	}


	public void handleControlContent(Control control) {
		controlReceived = true;
	}

	public boolean isControlReceived() {
		return controlReceived;
	}
}
