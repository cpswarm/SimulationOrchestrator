
package config.deployment;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * The Items Schema
 * <p>
 * 
 * 
 */
public class Service {

    /**
     * The Metadata Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("metadata")
    @Expose
    private Metadata__ metadata;
    /**
     * The Spec Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("spec")
    @Expose
    private Spec__ spec;
    /**
     * The Selector Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("selector")
    @Expose
    private Selector_ selector;
    /**
     * The Type Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("type")
    @Expose
    private String type = "";

    /**
     * The Metadata Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public Metadata__ getMetadata() {
        return metadata;
    }

    /**
     * The Metadata Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setMetadata(Metadata__ metadata) {
        this.metadata = metadata;
    }

    /**
     * The Spec Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public Spec__ getSpec() {
        return spec;
    }

    /**
     * The Spec Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setSpec(Spec__ spec) {
        this.spec = spec;
    }

    /**
     * The Selector Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public Selector_ getSelector() {
        return selector;
    }

    /**
     * The Selector Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setSelector(Selector_ selector) {
        this.selector = selector;
    }

    /**
     * The Type Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getType() {
        return type;
    }

    /**
     * The Type Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setType(String type) {
        this.type = type;
    }

}
