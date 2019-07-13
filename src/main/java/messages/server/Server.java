package messages.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * server
 * <p>
 * 
 * 
 */
public class Server implements Comparable<Server> {

    /**
     * ID of the simulation server
     * (Required)
     * 
     */
    @SerializedName("server")
    @Expose
    private String server;
    /**
     * SCID: Simulation Configuration IDentifier to be used to select this server
     * (Required)
     * 
     */
    @SerializedName("SCID")
    @Expose
    private String SCID = null;
    /**
     * Capabilities of the simulator
     * (Required)
     * 
     */
    @SerializedName("capabilities")
    @Expose
    private Capabilities capabilities;

    /**
     * ID of the simulation server
     * (Required)
     * 
     */
    public String getServer() {
        return server;
    }

    /**
     * ID of the simulation server
     * (Required)
     * 
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * SCID: new presence status to be used to select this server by OT
     * (Required)
     * 
     */
    public String getSCID() {
        return SCID;
    }

    /**
     * SCID: new presence status to be used to select this server by OT
     * (Required)
     * 
     */
    public void setSCID(String SCID) {
        this.SCID = SCID;
    }

    /**
     * Capabilities of the simulator
     * (Required)
     * 
     */
    public Capabilities getCapabilities() {
        return capabilities;
    }

    /**
     * Capabilities of the simulator
     * (Required)
     * 
     */
    public void setCapabilities(Capabilities capabilities) {
        this.capabilities = capabilities;
    }

	@Override
	public int compareTo(Server serverToCompare) {
		return this.getCapabilities().compareTo(serverToCompare.getCapabilities());
	}
    
    
}
