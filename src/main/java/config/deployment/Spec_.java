
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
public class Spec_ {

    /**
     * The Containers Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("containers")
    @Expose
    private List<Container> containers = null;
    /**
     * The Nodeselector Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("nodeSelector")
    @Expose
    private NodeSelector nodeSelector;

    /**
     * The Containers Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public List<Container> getContainers() {
        return containers;
    }

    /**
     * The Containers Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setContainers(List<Container> containers) {
        this.containers = containers;
    }

    /**
     * The Nodeselector Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public NodeSelector getNodeSelector() {
        return nodeSelector;
    }

    /**
     * The Nodeselector Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setNodeSelector(NodeSelector nodeSelector) {
        this.nodeSelector = nodeSelector;
    }

}
