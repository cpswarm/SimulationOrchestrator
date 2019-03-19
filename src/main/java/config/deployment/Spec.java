
package config.deployment;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * The Spec Schema
 * <p>
 * 
 * 
 */
public class Spec {

    /**
     * The Replicas Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("replicas")
    @Expose
    private Integer replicas = 0;
    /**
     * The Selector Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("selector")
    @Expose
    private Selector selector;

    /**
     * The Replicas Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public Integer getReplicas() {
        return replicas;
    }

    /**
     * The Replicas Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    /**
     * The Selector Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public Selector getSelector() {
        return selector;
    }

    /**
     * The Selector Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setSelector(Selector selector) {
        this.selector = selector;
    }

}
