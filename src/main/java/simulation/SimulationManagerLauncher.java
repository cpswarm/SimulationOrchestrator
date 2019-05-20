package simulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 
 */


/**
 * A Runnable class used to launch automatically the Simulation Manager
 *
 */
public class SimulationManagerLauncher implements Runnable {

	
	private boolean canRun = true;
	private String simulationManagerPath = null;
	private String simulationManagerParameters = null;
	
	public SimulationManagerLauncher(final String simulationManagerPath, final String simulationManagerParameters) {
		this.simulationManagerPath = simulationManagerPath;
		this.simulationManagerParameters = simulationManagerParameters;
	}
	
	@Override
	public void run() {
		try {
			System.out.println("Launching Simulation Manager with the following parameters: "+simulationManagerParameters) ;
			Process proc = Runtime.getRuntime().exec("java -jar "+ simulationManagerPath + " "+simulationManagerParameters);
			Runtime.getRuntime().addShutdownHook(new Thread(proc::destroy));
			String line = "";
			BufferedReader input =  
					new BufferedReader  
					(new InputStreamReader(proc.getInputStream()));  
			while ((line = input.readLine()) != null && this.canRun) {  
				System.out.println(line);  
			}  
			input.close();  
			Thread.sleep(2000);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}