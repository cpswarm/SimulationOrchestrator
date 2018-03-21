
package messages.server;

import java.util.Comparator;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * server
 * <p>
 * 
 * 
 */
public class Server implements Comparable<Server> {

    /**
     * ID of the simulation server
     * (Required)
     * 
     */
    @SerializedName("server")
    @Expose
    private Long server;
    /**
     * Hash of the simulation
     * (Required)
     * 
     */
    @SerializedName("simulation_hash")
    @Expose
    private String simulationHash;
    /**
     * A list of all the simulations that can be performed at this server
     * (Required)
     * 
     */
    @SerializedName("simulations")
    @Expose
    private List<String> simulations = null;
    /**
     * Capabilities of the simulator
     * (Required)
     * 
     */
    @SerializedName("capabilities")
    @Expose
    private Capabilities capabilities;

    /**
     * ID of the simulation server
     * (Required)
     * 
     */
    public Long getServer() {
        return server;
    }

    /**
     * ID of the simulation server
     * (Required)
     * 
     */
    public void setServer(Long server) {
        this.server = server;
    }

    /**
     * Hash of the simulation
     * (Required)
     * 
     */
    public String getSimulationHash() {
        return simulationHash;
    }

    /**
     * Hash of the simulation
     * (Required)
     * 
     */
    public void setSimulationHash(String simulationHash) {
        this.simulationHash = simulationHash;
    }

    /**
     * A list of all the simulations that can be performed at this server
     * (Required)
     * 
     */
    public List<String> getSimulations() {
        return simulations;
    }

    /**
     * A list of all the simulations that can be performed at this server
     * (Required)
     * 
     */
    public void setSimulations(List<String> simulations) {
        this.simulations = simulations;
    }

    /**
     * Capabilities of the simulator
     * (Required)
     * 
     */
    public Capabilities getCapabilities() {
        return capabilities;
    }

    /**
     * Capabilities of the simulator
     * (Required)
     * 
     */
    public void setCapabilities(Capabilities capabilities) {
        this.capabilities = capabilities;
    }

	@Override
	public int compareTo(Server serverToCompare) {
		return this.getCapabilities().compareTo(serverToCompare.getCapabilities());
	}
    
    
}
