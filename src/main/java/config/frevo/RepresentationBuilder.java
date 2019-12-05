
package config.frevo;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RepresentationBuilder {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("parameters")
    @Expose
    private List<Parameter> parameters = null;
    @SerializedName("inputCount")
    @Expose
    private Integer inputCount;
    @SerializedName("outputCount")
    @Expose
    private Integer outputCount;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public Integer getInputCount() {
        return inputCount;
    }

    public void setInputCount(Integer inputCount) {
        this.inputCount = inputCount;
    }

    public Integer getOutputCount() {
        return outputCount;
    }

    public void setOutputCount(Integer outputCount) {
        this.outputCount = outputCount;
    }

}
