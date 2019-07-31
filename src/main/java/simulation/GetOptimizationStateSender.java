package simulation;

public class GetOptimizationStateSender implements Runnable {

	private SimulationOrchestrator parent = null;
	private boolean sendState = true;
	private final static int TIME_TO_SLEEP = 60*1000;
	
	
	public GetOptimizationStateSender(final SimulationOrchestrator parent) {
		this.parent = parent;
	}
	
	@Override
	public void run() {
		while(sendState) {
			if(!parent.sendGetOptimizationState()) {
				this.setSendState(false);
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

}
