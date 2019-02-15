
package config.frevo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RepresentationBuilder {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("activationFunction")
    @Expose
    private String activationFunction;
    @SerializedName("hiddenNodeCount")
    @Expose
    private Integer hiddenNodeCount;
    @SerializedName("iterationCount")
    @Expose
    private Integer iterationCount;
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

    public String getActivationFunction() {
        return activationFunction;
    }

    public void setActivationFunction(String activationFunction) {
        this.activationFunction = activationFunction;
    }

    public Integer getHiddenNodeCount() {
        return hiddenNodeCount;
    }

    public void setHiddenNodeCount(Integer hiddenNodeCount) {
        this.hiddenNodeCount = hiddenNodeCount;
    }

    public Integer getIterationCount() {
        return iterationCount;
    }

    public void setIterationCount(Integer iterationCount) {
        this.iterationCount = iterationCount;
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
