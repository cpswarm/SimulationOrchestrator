
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
     * The Stage Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("stage")
    @Expose
    private String stage = "";

    /**
     * The Stage Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getStage() {
        return stage;
    }

    /**
     * The Stage Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setStage(String stage) {
        this.stage = stage;
    }

}
