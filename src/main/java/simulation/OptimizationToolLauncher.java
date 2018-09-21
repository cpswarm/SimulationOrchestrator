package simulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 
 */


/**
 * A Runnable class used to launch automatically the Optimization Tool
 *
 */
public class OptimizationToolLauncher implements Runnable {

	
	private boolean canRun = true;
	private String optimizationToolPath = null;
	private String optimizationToolParameters = null;
	
	public OptimizationToolLauncher(final String optimizationToolPath, final String optimizationToolParameters) {
		this.optimizationToolPath = optimizationToolPath;
		this.optimizationToolParameters = optimizationToolParameters;
	}
	
	@Override
	public void run() {
		try {
			System.out.println("Launching Optimization Tool with the following parameters: "+optimizationToolParameters) ;
			Process proc = Runtime.getRuntime().exec("java -jar "+optimizationToolPath + " "+optimizationToolParameters);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
