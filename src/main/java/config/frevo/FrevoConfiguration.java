
package config.frevo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FrevoConfiguration {

    @SerializedName("simulationTimeoutSeconds")
    @Expose
    private Integer simulationTimeoutSeconds;
    @SerializedName("evolutionSeed")
    @Expose
    private Integer evolutionSeed;
    @SerializedName("evaluationSeed")
    @Expose
    private Integer evaluationSeed;
    @SerializedName("generationCount")
    @Expose
    private Integer generationCount;
    @SerializedName("candidateCount")
    @Expose
    private Integer candidateCount;
    @SerializedName("problemBuilder")
    @Expose
    private ProblemBuilder problemBuilder;
    @SerializedName("representationBuilder")
    @Expose
    private RepresentationBuilder representationBuilder;
    @SerializedName("operatorBuilder")
    @Expose
    private OperatorBuilder operatorBuilder;
    @SerializedName("methodBuilder")
    @Expose
    private MethodBuilder methodBuilder;
    @SerializedName("executorBuilder")
    @Expose
    private ExecutorBuilder executorBuilder;

    public Integer getSimulationTimeoutSeconds() {
        return simulationTimeoutSeconds;
    }

    public void setSimulationTimeoutSeconds(Integer simulationTimeoutSeconds) {
        this.simulationTimeoutSeconds = simulationTimeoutSeconds;
    }

    public Integer getEvolutionSeed() {
        return evolutionSeed;
    }

    public void setEvolutionSeed(Integer evolutionSeed) {
        this.evolutionSeed = evolutionSeed;
    }

    public Integer getEvaluationSeed() {
        return evaluationSeed;
    }

    public void setEvaluationSeed(Integer evaluationSeed) {
        this.evaluationSeed = evaluationSeed;
    }

    public Integer getGenerationCount() {
        return generationCount;
    }

    public void setGenerationCount(Integer generationCount) {
        this.generationCount = generationCount;
    }

    public Integer getCandidateCount() {
        return candidateCount;
    }

    public void setCandidateCount(Integer candidateCount) {
        this.candidateCount = candidateCount;
    }

    public ProblemBuilder getProblemBuilder() {
        return problemBuilder;
    }

    public void setProblemBuilder(ProblemBuilder problemBuilder) {
        this.problemBuilder = problemBuilder;
    }

    public RepresentationBuilder getRepresentationBuilder() {
        return representationBuilder;
    }

    public void setRepresentationBuilder(RepresentationBuilder representationBuilder) {
        this.representationBuilder = representationBuilder;
    }

    public OperatorBuilder getOperatorBuilder() {
        return operatorBuilder;
    }

    public void setOperatorBuilder(OperatorBuilder operatorBuilder) {
        this.operatorBuilder = operatorBuilder;
    }

    public MethodBuilder getMethodBuilder() {
        return methodBuilder;
    }

    public void setMethodBuilder(MethodBuilder methodBuilder) {
        this.methodBuilder = methodBuilder;
    }

    public ExecutorBuilder getExecutorBuilder() {
        return executorBuilder;
    }

    public void setExecutorBuilder(ExecutorBuilder executorBuilder) {
        this.executorBuilder = executorBuilder;
    }

}
