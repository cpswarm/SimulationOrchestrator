
package config.deployment;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Requests {

    @SerializedName("cpu")
    @Expose
    private Integer cpu;
    @SerializedName("memory")
    @Expose
    private String memory;

    public Integer getCpu() {
        return cpu;
    }

    public void setCpu(Integer cpu) {
        this.cpu = cpu;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

}
