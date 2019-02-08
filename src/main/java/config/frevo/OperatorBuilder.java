
package config.frevo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OperatorBuilder {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("initialWeightRange")
    @Expose
    private Double initialWeightRange;
    @SerializedName("initialBiasRange")
    @Expose
    private Double initialBiasRange;
    @SerializedName("initialRandomBiasRange")
    @Expose
    private Double initialRandomBiasRange;
    @SerializedName("weightRange")
    @Expose
    private Double weightRange;
    @SerializedName("biasRange")
    @Expose
    private Double biasRange;
    @SerializedName("randomBiasRange")
    @Expose
    private Double randomBiasRange;
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

    public Double getInitialWeightRange() {
        return initialWeightRange;
    }

    public void setInitialWeightRange(Double initialWeightRange) {
        this.initialWeightRange = initialWeightRange;
    }

    public Double getInitialBiasRange() {
        return initialBiasRange;
    }

    public void setInitialBiasRange(Double initialBiasRange) {
        this.initialBiasRange = initialBiasRange;
    }

    public Double getInitialRandomBiasRange() {
        return initialRandomBiasRange;
    }

    public void setInitialRandomBiasRange(Double initialRandomBiasRange) {
        this.initialRandomBiasRange = initialRandomBiasRange;
    }

    public Double getWeightRange() {
        return weightRange;
    }

    public void setWeightRange(Double weightRange) {
        this.weightRange = weightRange;
    }

    public Double getBiasRange() {
        return biasRange;
    }

    public void setBiasRange(Double biasRange) {
        this.biasRange = biasRange;
    }

    public Double getRandomBiasRange() {
        return randomBiasRange;
    }

    public void setRandomBiasRange(Double randomBiasRange) {
        this.randomBiasRange = randomBiasRange;
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
