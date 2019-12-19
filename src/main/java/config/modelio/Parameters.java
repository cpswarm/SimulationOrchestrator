
package config.modelio;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Parameters {

    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("parameters")
    @Expose
    private List<Parameter> parameters = null;
    @SerializedName("additionalProperties")
    @Expose
    private Object additionalProperties;

    /**
     * 
     * (Required)
     * 
     */
    public List<Parameter> getParameters() {
        return parameters;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public Object getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Object additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

}
