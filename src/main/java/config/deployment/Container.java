
package config.deployment;

import java.util.List;
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
     * The Args Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("args")
    @Expose
    private List<String> args = null;
    /**
     * The resources Schema
     * <p>
     * 
     * (Not Required)
     * 
     */
    @SerializedName("resources")
    @Expose
    private Resources resources;
    /**
     * The Stdin Schema
     * <p>
     * 
     * (Not Required)
     * 
     */
    @SerializedName("stdin")
    @Expose
    private String stdin = "";
    /**
     * The Stdin Schema
     * <p>
     * 
     * (Not Required)
     * 
     */    
    @SerializedName("env")
    @Expose
    private List<Env> env = null;
    
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

    /**
     * The Args Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public List<String> getArgs() {
        return args;
    }

    /**
     * The Args Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setArgs(List<String> args) {
        this.args = args;
    }

    /**
     * The Resources Schema
     * <p>
     * 
     * (Not Required)
     * 
     */    
    public Resources getResources() {
        return resources;
    }

    /**
     * The Resources Schema
     * <p>
     * 
     * (Not Required)
     * 
     */
    public void setResources(Resources resources) {
        this.resources = resources;
    }

    
    /**
     * The Stdin Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getStdin() {
        return stdin;
    }

    /**
     * The Stdin Schema
     * <p>
     * 
     * (Not Required)
     * 
     */
    public void setStdin(String stdin) {
        this.stdin = stdin;
    }

    /**
     * The Env Schema
     * <p>
     * 
     * (Not Required)
     * 
     */
    public List<Env> getEnv() {
        return env;
    }

    /**
     * The Env Schema
     * <p>
     * 
     * (Not Required)
     * 
     */
    public void setEnv(List<Env> env) {
        this.env = env;
    }
}
