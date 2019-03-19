
package config.deployment;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * The Items Schema
 * <p>
 * 
 * 
 */
public class Deployment {

    /**
     * The Metadata Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("metadata")
    @Expose
    private Metadata metadata;
    /**
     * The Spec Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("spec")
    @Expose
    private Spec spec;
    /**
     * The Template Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("template")
    @Expose
    private Template template;

    /**
     * The Metadata Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * The Metadata Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * The Spec Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public Spec getSpec() {
        return spec;
    }

    /**
     * The Spec Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setSpec(Spec spec) {
        this.spec = spec;
    }

    /**
     * The Template Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * The Template Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setTemplate(Template template) {
        this.template = template;
    }

}
