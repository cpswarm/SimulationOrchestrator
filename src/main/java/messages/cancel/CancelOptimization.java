package messages.cancel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CancelOptimization {

	/**
	 * The Title Schema
	 * <p>
	 *
	 *
	 */
	@SerializedName("title")
	@Expose
	private String title = "CancelOptimization";
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

}