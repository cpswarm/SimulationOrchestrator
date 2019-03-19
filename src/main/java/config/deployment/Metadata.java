
package config.deployment;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * The Metadata Schema
 * <p>
 * 
 * 
 */
public class Metadata {

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
     * The Generation Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("generation")
    @Expose
    private Integer generation = 0;
    /**
     * The Labels Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("labels")
    @Expose
    private Labels labels;

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

    /**
     * The Generation Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public Integer getGeneration() {
        return generation;
    }

    /**
     * The Generation Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setGeneration(Integer generation) {
        this.generation = generation;
    }

    /**
     * The Labels Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public Labels getLabels() {
        return labels;
    }

    /**
     * The Labels Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setLabels(Labels labels) {
        this.labels = labels;
    }

}
