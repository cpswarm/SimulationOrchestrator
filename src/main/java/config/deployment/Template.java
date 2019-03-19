
package config.deployment;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * The Template Schema
 * <p>
 * 
 * 
 */
public class Template {

    /**
     * The Metadata Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("metadata")
    @Expose
    private Metadata_ metadata;
    /**
     * The Spec Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("spec")
    @Expose
    private Spec_ spec;

    /**
     * The Metadata Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public Metadata_ getMetadata() {
        return metadata;
    }

    /**
     * The Metadata Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setMetadata(Metadata_ metadata) {
        this.metadata = metadata;
    }

    /**
     * The Spec Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public Spec_ getSpec() {
        return spec;
    }

    /**
     * The Spec Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setSpec(Spec_ spec) {
        this.spec = spec;
    }

}
