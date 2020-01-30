
package config.deployment;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Resources {

    @SerializedName("requests")
    @Expose
    private Requests requests;
    @SerializedName("limits")
    @Expose
    private Limits limits;

    public Requests getRequests() {
        return requests;
    }

    public void setRequests(Requests requests) {
        this.requests = requests;
    }

    public Limits getLimits() {
        return limits;
    }

    public void setLimits(Limits limits) {
        this.limits = limits;
    }

}
