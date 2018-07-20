package messages.simulation;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RunSimulation {

	/**
	 * The Title Schema
	 * <p>
	 *
	 *
	 */
	@SerializedName("title")
	@Expose
	private String title = "RunSimulation";
	/**
	 * The Id Schema
	 * <p>
	 *
	 *
	 */
	@SerializedName("ID")
	@Expose
	private String iD = "";
	/**
	 * The Gui Schema
	 * <p>
	 *
	 *
	 */
	@SerializedName("gui")
	@Expose
	private Boolean gui = false;
	/**
	 * The Params Schema
	 * <p>
	 *
	 *
	 */
	@SerializedName("params")
	@Expose
	private String params = "";

	/**
	 * The Title Schema
	 * <p>
	 *
	 *
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * The Title Schema
	 * <p>
	 *
	 *
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * The Id Schema
	 * <p>
	 *
	 *
	 */
	public String getID() {
		return iD;
	}

	/**
	 * The Id Schema
	 * <p>
	 *
	 *
	 */
	public void setID(String iD) {
		this.iD = iD;
	}

	/**
	 * The Gui Schema
	 * <p>
	 *
	 *
	 */
	public Boolean getGui() {
		return gui;
	}

	/**
	 * The Gui Schema
	 * <p>
	 *
	 *
	 */
	public void setGui(Boolean gui) {
		this.gui = gui;
	}

	/**
	 * The Params Schema
	 * <p>
	 *
	 *
	 */
	public String getParams() {
		return params;
	}

	/**
	 * The Params Schema
	 * <p>
	 *
	 *
	 */
	public void setParams(String params) {
		this.params = params;
	}

}