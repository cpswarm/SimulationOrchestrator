package simulation;

public class GetOptimizationStatusSender implements Runnable {

	private SimulationOrchestrator parent = null;
	private boolean sendState = true;
	private boolean suspendState = false;
	private final static int TIME_TO_SLEEP = 30*1000;
	
	
	public GetOptimizationStatusSender(final SimulationOrchestrator parent) {
		this.parent = parent;
	}
	
	@Override
	public void run() {
		while(sendState) {
			if(!suspendState) {
				if(!parent.sendGetOptimizationStatus()) {
					this.setSendState(false);
				}
			}
			try {
				Thread.sleep(TIME_TO_SLEEP);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
		
	
	/**
	 * The runnable flag setter, if called with a false value, the thread will
	 * stop and gracefully exit.
	 * 
	 * @param canRun
	 */
	public synchronized void setSendState(boolean sendState) {
		this.sendState = sendState;
	}

	/**
	 * The suspend flag setter, if called with a true value, the thread will
	 * stop sending messages but will continue running
	 * 
	 * @param canRun
	 */
	public synchronized void setSuspendState(boolean suspendState) {
		this.suspendState = suspendState;
	}
	
	public boolean isSuspendState() {
		return suspendState;
	}
}
