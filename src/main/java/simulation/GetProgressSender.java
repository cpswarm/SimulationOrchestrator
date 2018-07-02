package simulation;
/**
 * 
 */


/**
 * A Runnable class for monitoring the progess of the optimization process
 * 
 *
 */
public class GetProgressSender implements Runnable {

	
	private SimulationOrchestrator parent = null;
	private boolean canRun = true;
	private final static int TIME_TO_SLEEP = 5*60*1000;
	
	
	public GetProgressSender(final SimulationOrchestrator parent) {
		this.parent = parent;
	}
	
	@Override
	public void run() {
		while(canRun) {
			parent.sendGetProgress();
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
