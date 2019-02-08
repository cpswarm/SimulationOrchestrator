
package config.frevo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ExecutorBuilder {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("threadCount")
    @Expose
    private Integer threadCount;
    @SerializedName("problemVariantCount")
    @Expose
    private Integer problemVariantCount;
    @SerializedName("poolType")
    @Expose
    private String poolType;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(Integer threadCount) {
        this.threadCount = threadCount;
    }

    public Integer getProblemVariantCount() {
        return problemVariantCount;
    }

    public void setProblemVariantCount(Integer problemVariantCount) {
        this.problemVariantCount = problemVariantCount;
    }

    public String getPoolType() {
        return poolType;
    }

    public void setPoolType(String poolType) {
        this.poolType = poolType;
    }

}
