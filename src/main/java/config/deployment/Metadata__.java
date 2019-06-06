
package config.deployment;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * The Metadata Schema
 * <p>
 * 
 * 
 */
public class Metadata__ {

    /**
     * The Application Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("application")
    @Expose
    private String application = "";
    /**
     * The Name Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("name")
    @Expose
    private String name = "";
    /**
     * The Namespace Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("namespace")
    @Expose
    private String namespace = "";
    
    /**
     * The Application Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getApplication() {
        return application;
    }

    /**
     * The Application Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setApplication(String application) {
        this.application = application;
    }

    /**
     * The Name Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getName() {
        return name;
    }

    /**
     * The Name Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Namespace Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * The Namespace Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
