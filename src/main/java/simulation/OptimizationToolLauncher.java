package simulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 
 */


/**
 * A Runnable class for monitoring the progess of the optimization process
 * 
 *
 */
public class OptimizationToolLauncher implements Runnable {

	
	private SimulationOrchestrator parent = null;
	private boolean canRun = true;
	private String optimizationToolPath = null;
	
	public OptimizationToolLauncher(final SimulationOrchestrator parent, final String optimizationToolPath) {
		this.parent = parent;
		this.optimizationToolPath = optimizationToolPath;
	}
	
	@Override
	public void run() {
		while(canRun) {
			try {
				System.out.println("Launching Optimization Tool");
				Process proc = Runtime.getRuntime().exec("java -jar "+optimizationToolPath);

				Runtime.getRuntime().addShutdownHook(new Thread(proc::destroy));
				String line = "";
				BufferedReader input =  
						new BufferedReader  
						(new InputStreamReader(proc.getErrorStream()));  
				while ((line = input.readLine()) != null) {  
					System.out.println(line);  
				}  
				input.close();  
				Thread.sleep(2000);
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
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
