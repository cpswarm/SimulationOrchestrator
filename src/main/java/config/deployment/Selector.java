
package config.deployment;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * The Selector Schema
 * <p>
 * 
 * 
 */
public class Selector {

    /**
     * The Matchlabels Schema
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("matchLabels")
    @Expose
    private MatchLabels matchLabels;

    /**
     * The Matchlabels Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public MatchLabels getMatchLabels() {
        return matchLabels;
    }

    /**
     * The Matchlabels Schema
     * <p>
     * 
     * (Required)
     * 
     */
    public void setMatchLabels(MatchLabels matchLabels) {
        this.matchLabels = matchLabels;
    }

}
