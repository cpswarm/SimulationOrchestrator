
package messages.control;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * control
 * <p>
 * 
 * 
 */
public class Control {

    /**
     * ID of the simulation server
     * (Required)
     * 
     */
    @SerializedName("server")
    @Expose
    private Integer server;
    /**
     * Hash of the simulation
     * 
     */
    @SerializedName("simulation_hash")
    @Expose
    private String simulationHash;
    /**
     * Whether to run or terminate a simulation
     * 
     */
    @SerializedName("run")
    @Expose
    private Boolean run = true;
    /**
     * Whether to run the simulation headless or using a GUI
     * 
     */
    @SerializedName("visual")
    @Expose
    private Boolean visual = false;

    /**
     * ID of the simulation server
     * (Required)
     * 
     */
    public Integer getServer() {
        return server;
    }

    /**
     * ID of the simulation server
     * (Required)
     * 
     */
    public void setServer(Integer server) {
        this.server = server;
    }

    /**
     * Hash of the simulation
     * 
     */
    public String getSimulationHash() {
        return simulationHash;
    }

    /**
     * Hash of the simulation
     * 
     */
    public void setSimulationHash(String simulationHash) {
        this.simulationHash = simulationHash;
    }

    /**
     * Whether to run or terminate a simulation
     * 
     */
    public Boolean getRun() {
        return run;
    }

    /**
     * Whether to run or terminate a simulation
     * 
     */
    public void setRun(Boolean run) {
        this.run = run;
    }

    /**
     * Whether to run the simulation headless or using a GUI
     * 
     */
    public Boolean getVisual() {
        return visual;
    }

    /**
     * Whether to run the simulation headless or using a GUI
     * 
     */
    public void setVisual(Boolean visual) {
        this.visual = visual;
    }

}
