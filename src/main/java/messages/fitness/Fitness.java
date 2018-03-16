
package messages.fitness;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * fitness
 * <p>
 * 
 * 
 */
public class Fitness {

    /**
     * Hash of the simulation
     * 
     */
    @SerializedName("simulation_hash")
    @Expose
    private String simulationHash;
    /**
     * Fitness score of the candidate controller used in the simulation
     * (Required)
     * 
     */
    @SerializedName("fitness")
    @Expose
    private Double fitness;

    /**
     * Hash of the simulation
     * 
     */
    public String getSimulationHash() {
        return simulationHash;
    }

    /**
     * Hash of the simulation
     * 
     */
    public void setSimulationHash(String simulationHash) {
        this.simulationHash = simulationHash;
    }

    /**
     * Fitness score of the candidate controller used in the simulation
     * (Required)
     * 
     */
    public Double getFitness() {
        return fitness;
    }

    /**
     * Fitness score of the candidate controller used in the simulation
     * (Required)
     * 
     */
    public void setFitness(Double fitness) {
        this.fitness = fitness;
    }

}
