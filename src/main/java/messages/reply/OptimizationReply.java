package messages.reply;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OptimizationReply {

	public static final String OPTIMIZATION_STARTED = "optimizationStarted";
	public static final String OPTIMIZATION_CANCELLED = "optimizationCancelled";
	public static final String OPTIMIZATION_PROGRESS = "optimizationProgress";
	
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
* The Operationstatus Schema
* <p>
*
*
*/
@SerializedName("operationStatus")
@Expose
private String operationStatus = "";

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

}
