package simulation;

public class GetOptimizationStatusSender implements Runnable {

	private SimulationOrchestrator parent = null;
	private boolean sendState = true;
	private boolean suspendState = false;
	private final int TIME_TO_SLEEP;
	
	
	public GetOptimizationStatusSender(final SimulationOrchestrator parent) {
		this.parent = parent;
		this.TIME_TO_SLEEP = (int) (parent.getSimulationTimeoutSeconds()*0.8*1000);
	}
	
	@Override
	public void run() {
		while(sendState) {
			if(!suspendState) {
			//	System.out.println("status sender is asking for the status.....");
				if(!parent.sendGetOptimizationStatus()) {
					this.setSuspendState(true);
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
