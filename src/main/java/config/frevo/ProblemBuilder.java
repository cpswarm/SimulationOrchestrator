
package config.frevo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProblemBuilder {

    @SerializedName("representationInputCount")
    @Expose
    private Integer representationInputCount;
    @SerializedName("representationOutputCount")
    @Expose
    private Integer representationOutputCount;
    @SerializedName("maximumFitness")
    @Expose
    private Double maximumFitness;

    public Integer getRepresentationInputCount() {
        return representationInputCount;
    }

    public void setRepresentationInputCount(Integer representationInputCount) {
        this.representationInputCount = representationInputCount;
    }

    public Integer getRepresentationOutputCount() {
        return representationOutputCount;
    }

    public void setRepresentationOutputCount(Integer representationOutputCount) {
        this.representationOutputCount = representationOutputCount;
    }

    public Double getMaximumFitness() {
        return maximumFitness;
    }

    public void setMaximumFitness(Double maximumFitness) {
        this.maximumFitness = maximumFitness;
    }

}
