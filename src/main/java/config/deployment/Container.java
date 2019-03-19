
package config.deployment;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * The Items Schema
 * <p>
 * 
 * 
 */
public class Container {

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
     * The Image Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("image")
    @Expose
    private String image = "";

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
     * The Image Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getImage() {
        return image;
    }

    /**
     * The Image Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setImage(String image) {
        this.image = image;
    }

}
