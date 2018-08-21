package messages.result;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OptimizationResult {

	/**
	 * The Title Schema
	 * <p>
	 *
	 *
	 */
	@SerializedName("title")
	@Expose
	private String title = "";
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
	 * The Fitnessvalue Schema
	 * <p>
	 *
	 *
	 */
	@SerializedName("fitnessValue")
	@Expose
	private Double fitnessValue = 0.0D;

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
	 * The Fitnessvalue Schema
	 * <p>
	 *
	 *
	 */
	public Double getFitnessValue() {
		return fitnessValue;
	}

	/**
	 * The Fitnessvalue Schema
	 * <p>
	 *
	 *
	 */
	public void setFitnessValue(Double fitnessValue) {
		this.fitnessValue = fitnessValue;
	}

}