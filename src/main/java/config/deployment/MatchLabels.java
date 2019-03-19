
package config.deployment;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * The Matchlabels Schema
 * <p>
 * 
 * 
 */
public class MatchLabels {

    /**
     * The K8s-app Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("k8s-app")
    @Expose
    private String k8sApp = "";

    /**
     * The K8s-app Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getK8sApp() {
        return k8sApp;
    }

    /**
     * The K8s-app Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setK8sApp(String k8sApp) {
        this.k8sApp = k8sApp;
    }

}
