package messages.progress;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OptimizationProgress {

	/**
	 * The Title Schema
	 * <p>
	 *
	 *
	 */
	@SerializedName("title")
	@Expose
	private String title = "OptimizationResult";

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
	 * The Operationstatus Schema
	 * <p>
	 *
	 *
	 */
	@SerializedName("operationStatus")
	@Expose
	private String operationStatus = "";

	/**
	 * The uom Schema
	 * <p>
	 *
	 *
	 */
	@SerializedName("uom")
	@Expose
	private String uom = "";

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
	 * The Operationstatus Schema
	 * <p>
	 *
	 *
	 */
	public String getOperationStatus() {
		return operationStatus;
	}

	/**
	 * The Operationstatus Schema
	 * <p>
	 *
	 *
	 */
	public void setOperationStatus(String operationStatus) {
		this.operationStatus = operationStatus;
	}

	/**
	 * The uom Schema
	 * <p>
	 *
	 *
	 */
	public String getUom() {
		return uom;
	}

	/**
	 * The uom Schema
	 * <p>
	 *
	 *
	 */
	public void setUom(String uom) {
		this.uom = uom;
	}
}