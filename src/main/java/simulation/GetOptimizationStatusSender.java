package simulation;
/**
 * 
 */


/**
 * A Runnable class for monitoring the progress of the optimization process
 * 
 *
 */
public class GetOptimizationStatusSender implements Runnable {

	
	private SimulationOrchestrator parent = null;
	private boolean canRun = true;
	private final static int TIME_TO_SLEEP = 60*1000;
	
	
	public GetOptimizationStatusSender(final SimulationOrchestrator parent) {
		this.parent = parent;
	}
	
	@Override
	public void run() {
		while(canRun) {
			if(!parent.sendGetOptimizationStatus()) {
				canRun = false;
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
	public synchronized void setCanRun(boolean canRun) {
		this.canRun = canRun;
	}	
}