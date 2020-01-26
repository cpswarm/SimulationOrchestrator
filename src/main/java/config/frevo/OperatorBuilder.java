
package config.frevo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OperatorBuilder {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("directMutationProbability")
    @Expose
    private Double directMutationProbability;
    @SerializedName("directMutationSeverity")
    @Expose
    private Double directMutationSeverity;
    @SerializedName("proportionalMutationProbability")
    @Expose
    private Double proportionalMutationProbability;
    @SerializedName("proportionalMutationSeverity")
    @Expose
    private Double proportionalMutationSeverity;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getDirectMutationProbability() {
        return directMutationProbability;
    }

    public void setDirectMutationProbability(Double directMutationProbability) {
        this.directMutationProbability = directMutationProbability;
    }

    public Double getDirectMutationSeverity() {
        return directMutationSeverity;
    }

    public void setDirectMutationSeverity(Double directMutationSeverity) {
        this.directMutationSeverity = directMutationSeverity;
    }

    public Double getProportionalMutationProbability() {
        return proportionalMutationProbability;
    }

    public void setProportionalMutationProbability(Double proportionalMutationProbability) {
        this.proportionalMutationProbability = proportionalMutationProbability;
    }

    public Double getProportionalMutationSeverity() {
        return proportionalMutationSeverity;
    }

    public void setProportionalMutationSeverity(Double proportionalMutationSeverity) {
        this.proportionalMutationSeverity = proportionalMutationSeverity;
    }

}
