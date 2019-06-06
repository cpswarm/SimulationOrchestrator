
package config.deployment;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * The Items Schema
 * <p>
 * 
 * 
 */
public class Port {

    /**
     * The Name Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("name")
    @Expose
    private String name = "";
    /**
     * The Protocol Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("protocol")
    @Expose
    private String protocol = "";
    /**
     * The Port Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("port")
    @Expose
    private Integer port = 0;
    /**
     * The Targetport Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("targetPort")
    @Expose
    private Integer targetPort = 0;
    /**
     * The Nodeport Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("nodePort")
    @Expose
    private Integer nodePort = 0;

    /**
     * The Name Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getName() {
        return name;
    }

    /**
     * The Name Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Protocol Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * The Protocol Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * The Port Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public Integer getPort() {
        return port;
    }

    /**
     * The Port Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * The Targetport Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public Integer getTargetPort() {
        return targetPort;
    }

    /**
     * The Targetport Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setTargetPort(Integer targetPort) {
        this.targetPort = targetPort;
    }

    /**
     * The Nodeport Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public Integer getNodePort() {
        return nodePort;
    }

    /**
     * The Nodeport Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setNodePort(Integer nodePort) {
        this.nodePort = nodePort;
    }

}
