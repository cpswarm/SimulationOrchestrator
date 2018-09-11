
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
     * Used to receive the configuration done using the Launcher
     */
    public void setDimensions(String dimensions) {
		Long dims = Long.valueOf(0);
		switch(dimensions.toUpperCase()) {
		case "ANY" : {
			dims = Long.valueOf(1);
			break;
		}
		case "2D":
		{	
			dims = Long.valueOf(2);
			break;
		}
		case "3D": {
			dims = Long.valueOf(3);
			break;
		}
		}
		this.setDimensions(dims);
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
		if((this.getDimensions()==capabilitiesToCompare.getDimensions() || this.getDimensions()==1) &&
				(this.getMaxAgents()==null || this.getMaxAgents()>=capabilitiesToCompare.getMaxAgents())) {
			return 1;
		} else {
			return -1;
		}
	}

}
