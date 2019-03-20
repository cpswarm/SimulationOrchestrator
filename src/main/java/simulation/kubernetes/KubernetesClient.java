package simulation.kubernetes;

public class KubernetesClient {
	/*
	ExtensionsV1beta1Api extsApi = new ExtensionsV1beta1Api();
	ExtensionsV1beta1Deployment deployment = new ExtensionsV1beta1Deployment(); 
	deployment.setKind("Deployment");
	deployment.setApiVersion("extensions/v1beta1");

	V1ObjectMeta meta = new V1ObjectMeta();
	meta.setName("test");
	meta.setNamespace("default");
	meta.setSelfLink("/apis/extensions/v1beta1/namespaces/default/deployments/test");
	meta.setUid("8b781241-d324-11e8-ab6c-34e6d7414fa1");
	meta.setResourceVersion("36621");
	meta.setGeneration(Long.valueOf(2));
	meta.setCreationTimestamp(DateTime.parse("2018-10-18T21:12:39Z"));
	Map<String,String> labelsMap = new HashMap<String,String>();
	labelsMap.put("k8s-app", "test");
	meta.setLabels(labelsMap);
	Map<String,String> annotationsMap = new HashMap<String,String>();
	annotationsMap.put("deployment.kubernetes.io/revision", "1");
	meta.setAnnotations(annotationsMap);
	meta.setResourceVersion(null);
	deployment.setMetadata(meta);
	
	ExtensionsV1beta1DeploymentSpec spec = new ExtensionsV1beta1DeploymentSpec();
	spec.setReplicas(1);
	V1LabelSelector labelSelector = new V1LabelSelector();
	labelSelector.putMatchLabelsItem("k8s-app", "test");
	spec.setSelector(labelSelector);
	V1PodTemplateSpec template = new V1PodTemplateSpec();
	V1ObjectMeta podMeta = new V1ObjectMeta();
	podMeta.setName("test");
	podMeta.setCreationTimestamp(null);
	podMeta.setLabels(labelsMap);
	template.setMetadata(podMeta);
	spec.setTemplate(template);
	V1PodSpec podSpec = new V1PodSpec();
	List<V1Container> containersList = new ArrayList<V1Container>();
	V1Container container = new V1Container();
	container.setName("test");
	container.setImage("dconzon/gazebo-simulation-manager:1.0.1");
	//V1ResourceRequirements resources = new V1ResourceRequirements();
	//container.setResources(resources);
	container.setTerminationMessagePath("/dev/termination-log");
	container.setTerminationMessagePolicy("File");
	container.setImagePullPolicy("IfNotPresent");
	V1SecurityContext secContext = new V1SecurityContext();
	secContext.setPrivileged(false);
	container.setSecurityContext(secContext);
	containersList.add(container);
	podSpec.setContainers(containersList);
	podSpec.setRestartPolicy("Always");
	podSpec.setTerminationGracePeriodSeconds(Long.valueOf(30));
	podSpec.setDnsPolicy("ClusterFirst");
	V1PodSecurityContext podSecContext = new V1PodSecurityContext();
	podSpec.setSecurityContext(podSecContext);
	podSpec.setSchedulerName("default-scheduler");
	template.setSpec(podSpec);
	ExtensionsV1beta1DeploymentStrategy strategy = new ExtensionsV1beta1DeploymentStrategy();
	strategy.setType("RollingUpdate");
	ExtensionsV1beta1RollingUpdateDeployment rollingUpdate = new ExtensionsV1beta1RollingUpdateDeployment();
	rollingUpdate.setMaxUnavailable("25%");
	rollingUpdate.setMaxSurge("25%");
	strategy .setRollingUpdate(rollingUpdate);
	spec.setStrategy(strategy);
	spec.setRevisionHistoryLimit(10);
	spec.setProgressDeadlineSeconds(600);
	deployment.setSpec(spec);
	
	ExtensionsV1beta1DeploymentStatus status = new ExtensionsV1beta1DeploymentStatus();
	status.setObservedGeneration(Long.valueOf(1));
	status.setReplicas(1);
	status.setUpdatedReplicas(1);
	status.setReadyReplicas(1);
	status.setAvailableReplicas(1);
	ExtensionsV1beta1DeploymentCondition progressingCondition = new ExtensionsV1beta1DeploymentCondition();
	progressingCondition.setType("Progressing");
	progressingCondition.setStatus("true");
	progressingCondition.setLastUpdateTime(DateTime.parse("2018-10-18T21:13:07Z"));
	progressingCondition.setLastUpdateTime(DateTime.parse("2018-10-18T21:12:39Z"));
	progressingCondition.setReason("NewReplicaSetAvailable");
	progressingCondition.setMessage("ReplicaSet \"test-97956575b\" has successfully progressed.");
	ExtensionsV1beta1DeploymentCondition availableCondition = new ExtensionsV1beta1DeploymentCondition();
	availableCondition.setType("Available");
	availableCondition.setStatus("true");
	availableCondition.setLastUpdateTime(DateTime.parse("2018-10-19T14:59:25Z"));
	availableCondition.setLastUpdateTime(DateTime.parse("2018-10-19T14:59:25Z"));
	availableCondition.setReason("MinimumReplicasAvailable");
	availableCondition.setMessage("Deployment has minimum availability.");
	List<ExtensionsV1beta1DeploymentCondition> conditions = new ArrayList<ExtensionsV1beta1DeploymentCondition>();
	conditions.add(progressingCondition);
	conditions.add(availableCondition);
	status.setConditions(conditions);
	deployment.setStatus(status);
	
	io.kubernetes.client.JSON json = new io.kubernetes.client.JSON();
	String jsonText1 = json.serialize(deployment);
	System.out.println(jsonText1);
	
	extsApi.createNamespacedDeployment("default", deployment, "true");

	
	try {
		Thread.sleep(10000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	ExtensionsV1beta1Scale body = new ExtensionsV1beta1Scale();
	body.setApiVersion("extensions/v1beta1");
	body.setKind("Scale");
	body.setMetadata(deployment.getMetadata());
	ExtensionsV1beta1ScaleSpec scale = new ExtensionsV1beta1ScaleSpec();
	scale.setReplicas(2);
	body.setSpec(scale);
	extsApi.replaceNamespacedDeploymentScale("test", "default", body, "true");
*/
}
