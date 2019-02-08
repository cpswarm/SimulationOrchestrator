
package config.frevo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MethodBuilder {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("skewFactor")
    @Expose
    private Double skewFactor;
    @SerializedName("eliteWeight")
    @Expose
    private Double eliteWeight;
    @SerializedName("randomWeight")
    @Expose
    private Double randomWeight;
    @SerializedName("mutatedWeight")
    @Expose
    private Double mutatedWeight;
    @SerializedName("crossedWeight")
    @Expose
    private Double crossedWeight;
    @SerializedName("newWeight")
    @Expose
    private Double newWeight;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getSkewFactor() {
        return skewFactor;
    }

    public void setSkewFactor(Double skewFactor) {
        this.skewFactor = skewFactor;
    }

    public Double getEliteWeight() {
        return eliteWeight;
    }

    public void setEliteWeight(Double eliteWeight) {
        this.eliteWeight = eliteWeight;
    }

    public Double getRandomWeight() {
        return randomWeight;
    }

    public void setRandomWeight(Double randomWeight) {
        this.randomWeight = randomWeight;
    }

    public Double getMutatedWeight() {
        return mutatedWeight;
    }

    public void setMutatedWeight(Double mutatedWeight) {
        this.mutatedWeight = mutatedWeight;
    }

    public Double getCrossedWeight() {
        return crossedWeight;
    }

    public void setCrossedWeight(Double crossedWeight) {
        this.crossedWeight = crossedWeight;
    }

    public Double getNewWeight() {
        return newWeight;
    }

    public void setNewWeight(Double newWeight) {
        this.newWeight = newWeight;
    }

}
