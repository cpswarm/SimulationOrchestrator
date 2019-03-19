
package config.deployment;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * The Root Schema
 * <p>
 * 
 * 
 */
public class DeploymentConfiguration {

    /**
     * The Deployments Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("deployments")
    @Expose
    private List<Deployment> deployments = null;

    /**
     * The Deployments Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public List<Deployment> getDeployments() {
        return deployments;
    }

    /**
     * The Deployments Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setDeployments(List<Deployment> deployments) {
        this.deployments = deployments;
    }

}
