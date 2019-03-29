
package config.deployment;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * The Nodeselector Schema
 * <p>
 * 
 * 
 */
public class NodeSelector {

    /**
     * The Component Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("component")
    @Expose
    private String component = "";

    /**
     * The Component Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getComponent() {
        return component;
    }

    /**
     * The Component Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setComponent(String component) {
        this.component = component;
    }

}
