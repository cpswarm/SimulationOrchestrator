package simulation.kubernetes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import config.deployment.Container;
import config.deployment.Deployment;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.models.ExtensionsV1beta1Deployment;
import io.kubernetes.client.models.ExtensionsV1beta1DeploymentSpec;
import io.kubernetes.client.models.ExtensionsV1beta1DeploymentStrategy;
import io.kubernetes.client.models.ExtensionsV1beta1RollingUpdateDeployment;
import io.kubernetes.client.models.ExtensionsV1beta1Scale;
import io.kubernetes.client.models.ExtensionsV1beta1ScaleSpec;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1LabelSelector;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1PodSecurityContext;
import io.kubernetes.client.models.V1PodSpec;
import io.kubernetes.client.models.V1PodTemplateSpec;
import io.kubernetes.client.models.V1SecurityContext;
import io.kubernetes.client.util.Config;

public final class KubernetesUtils {
	/**
	 * Method used to deploy a Kubernetes APP
	 * 
	 * @param deploy
	 * 		characteristics of the APP to be deployed
	 * @return true if all is OK, false otherwise
	 */
	public static boolean deploy(Deployment deploy) {
		ExtensionsV1beta1Deployment deployment = new ExtensionsV1beta1Deployment(); 
		deployment.setKind("Deployment");
		deployment.setApiVersion("extensions/v1beta1");

		V1ObjectMeta meta = new V1ObjectMeta();
		meta.setName(deploy.getMetadata().getName());
		meta.setNamespace(deploy.getMetadata().getNamespace());
		meta.setSelfLink("/apis/extensions/v1beta1/namespaces/default/deployments/"+deploy.getMetadata().getName());
		meta.setUid(UUID.randomUUID().toString());
		meta.setGeneration(Long.valueOf(1));
		Map<String,String> labelsMap = new HashMap<String,String>();
		labelsMap.put("k8s-app", deploy.getMetadata().getLabels().getK8sApp());
		meta.setLabels(labelsMap);
		Map<String,String> annotationsMap = new HashMap<String,String>();
		annotationsMap.put("deployment.kubernetes.io/revision", "1");
		meta.setAnnotations(annotationsMap);
		deployment.setMetadata(meta);

		ExtensionsV1beta1DeploymentSpec spec = new ExtensionsV1beta1DeploymentSpec();
		spec.setReplicas(deploy.getSpec().getReplicas());
		V1LabelSelector labelSelector = new V1LabelSelector();
		labelSelector.putMatchLabelsItem("k8s-app", deploy.getSpec().getSelector().getMatchLabels().getK8sApp());
		spec.setSelector(labelSelector);
		V1PodTemplateSpec template = new V1PodTemplateSpec();
		V1ObjectMeta podMeta = new V1ObjectMeta();
		podMeta.setName(deploy.getTemplate().getMetadata().getName());
		podMeta.setLabels(labelsMap);
		template.setMetadata(podMeta);
		spec.setTemplate(template);
		V1PodSpec podSpec = new V1PodSpec();
		List<V1Container> containersList = new ArrayList<V1Container>();
		for (Container c : deploy.getTemplate().getSpec().getContainers()) {
			V1Container container = new V1Container();
			container.setName(c.getName());
			container.setImage(c.getImage());
			container.setTerminationMessagePath("/dev/termination-log");
			container.setTerminationMessagePolicy("File");
			container.setImagePullPolicy("IfNotPresent");
			V1SecurityContext secContext = new V1SecurityContext();
			secContext.setPrivileged(false);
			container.setSecurityContext(secContext);
			containersList.add(container);
		}
		Map nodeSelector = new HashMap();
		podSpec.setNodeSelector(nodeSelector);
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

		/*
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
		*/
		
		try {
			ApiClient client = Config.defaultClient();
			Configuration.setDefaultApiClient(client);

			ExtensionsV1beta1Api extsApi = new ExtensionsV1beta1Api();
			ExtensionsV1beta1Deployment result = extsApi.createNamespacedDeployment("default", deployment, "true");
			io.kubernetes.client.JSON json = new io.kubernetes.client.JSON();
			String resultJson = json.serialize(result);
			System.out.println(resultJson);
		} catch (ApiException | IOException e) {
			System.out.println("Error deploying a simulator "+deployment.getMetadata().getName());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static boolean scale(final V1ObjectMeta metadata, Integer replicas) {
		ExtensionsV1beta1Api extsApi = new ExtensionsV1beta1Api();
		
		ExtensionsV1beta1Scale body = new ExtensionsV1beta1Scale();
		body.setApiVersion("extensions/v1beta1");
		body.setKind("Scale");
		body.setMetadata(metadata);
		ExtensionsV1beta1ScaleSpec scale = new ExtensionsV1beta1ScaleSpec();
		scale.setReplicas(replicas);
		body.setSpec(scale);
		try {
			ApiClient client = Config.defaultClient();
			Configuration.setDefaultApiClient(client);
			ExtensionsV1beta1Scale result = extsApi.replaceNamespacedDeploymentScale("test", "default", body, "true");
			io.kubernetes.client.JSON json = new io.kubernetes.client.JSON();
			String resultJson = json.serialize(result);
			System.out.println(resultJson);			
		} catch (ApiException | IOException e) {
			System.out.println("Error scaling the deployment "+metadata.getName());
			e.printStackTrace();
		}
		return true;		
	}
}
