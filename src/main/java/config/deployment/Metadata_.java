
package config.deployment;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * The Metadata Schema
 * <p>
 * 
 * 
 */
public class Metadata_ {

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
     * The Labels Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("labels")
    @Expose
    private Labels_ labels;

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
     * The Labels Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public Labels_ getLabels() {
        return labels;
    }

    /**
     * The Labels Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setLabels(Labels_ labels) {
        this.labels = labels;
    }

}
