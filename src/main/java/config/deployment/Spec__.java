
package config.deployment;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * The Spec Schema
 * <p>
 * 
 * 
 */
public class Spec__ {

    /**
     * The Ports Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("ports")
    @Expose
    private List<Port> ports = null;

    /**
     * The Ports Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public List<Port> getPorts() {
        return ports;
    }

    /**
     * The Ports Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setPorts(List<Port> ports) {
        this.ports = ports;
    }

}
