
package config.deployment;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * The Selector Schema
 * <p>
 * 
 * 
 */
public class Selector_ {

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

}
