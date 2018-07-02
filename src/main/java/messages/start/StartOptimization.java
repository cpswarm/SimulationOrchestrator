package messages.start;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StartOptimization {

	/**
	 * The Title Schema
	 * <p>
	 *
	 *
	 */
	@SerializedName("title")
	@Expose
	private String title = "StartOptimization";
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
	 * The Threads Schema
	 * <p>
	 *
	 *
	 */
	@SerializedName("threads")
	@Expose
	private Integer threads = 0;
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
	@SerializedName("SimulationManagers")
	@Expose
	private List<String> simulationManagers = null;

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
	 * The Threads Schema
	 * <p>
	 *
	 *
	 */
	public Integer getThreads() {
		return threads;
	}

	/**
	 * The Threads Schema
	 * <p>
	 *
	 *
	 */
	public void setThreads(Integer threads) {
		this.threads = threads;
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

	public List<String> getSimulationManagers() {
		return simulationManagers;
	}

	public void setSimulationManagers(List<String> simulationManagers) {
		this.simulationManagers = simulationManagers;
	}

}
