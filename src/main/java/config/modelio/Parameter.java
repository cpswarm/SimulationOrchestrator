
package config.modelio;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Parameter {

    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("name")
    @Expose
    private String name;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("meta")
    @Expose
    private String meta;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("min")
    @Expose
    private Double min;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("max")
    @Expose
    private Double max;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("scale")
    @Expose
    private String scale;

    /**
     * 
     * (Required)
     * 
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getMeta() {
        return meta;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setMeta(String meta) {
        this.meta = meta;
    }

    /**
     * 
     * (Required)
     * 
     */
    public Double getMin() {
        return min;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setMin(Double min) {
        this.min = min;
    }

    /**
     * 
     * (Required)
     * 
     */
    public Double getMax() {
        return max;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setMax(Double max) {
        this.max = max;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getScale() {
        return scale;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setScale(String scale) {
        this.scale = scale;
    }

}
