
package messages.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * Capabilities of the simulator
 * 
 */
public class Capabilities implements Comparable<Capabilities>{

    /**
     * Number of spatial dimensions in the simulator
     * (Required)
     * 
     */
    @SerializedName("dimensions")
    @Expose
    private Long dimensions;
    /**
     * Maximum number of agents supported by the simulator
     * 
     */
    @SerializedName("max_agents")
    @Expose
    private Long maxAgents;

    /**
     * Number of spatial dimensions in the simulator
     * (Required)
     * 
     */
    public Long getDimensions() {
        return dimensions;
    }

    /**
     * Number of spatial dimensions in the simulator
     * (Required)
     * 
     */
    public void setDimensions(Long dimensions) {
        this.dimensions = dimensions;
    }

    /**
     * Maximum number of agents supported by the simulator
     * 
     */
    public Long getMaxAgents() {
        return maxAgents;
    }

    /**
     * Maximum number of agents supported by the simulator
     * 
     */
    public void setMaxAgents(Long maxAgents) {
        this.maxAgents = maxAgents;
    }

	@Override
	public int compareTo(Capabilities capabilitiesToCompare) {
		if(this.getDimensions()>=capabilitiesToCompare.getDimensions() &&
				(this.getMaxAgents()==null || this.getMaxAgents()>=capabilitiesToCompare.getMaxAgents())) {
			return 1;
		} else {
			return -1;
		}
	}

}
