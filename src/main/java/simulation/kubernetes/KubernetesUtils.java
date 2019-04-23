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
		ExtensionsV1beta1Deployment currentDeployment = null;
		ExtensionsV1beta1Deployment deploymentToDo = new ExtensionsV1beta1Deployment(); 
		try {
			// Check the current status of the deployment
			ApiClient client = Config.defaultClient();
			Configuration.setDefaultApiClient(client);
			ExtensionsV1beta1Api extsApi = new ExtensionsV1beta1Api();
			try {
				currentDeployment = extsApi.readNamespacedDeploymentStatus(deploy.getMetadata().getName(), deploy.getMetadata().getNamespace(), "true");
			} catch(ApiException ex) {
				if(ex.getCode()!=404) {
					System.out.println("Exception checking the current deployment status for "+deploy.getMetadata().getName());
					return false;
				}
			}
			if(currentDeployment!=null) {
				// If something is already deployed 
				// but the current number of replica is not equal to the desired one
				// it scales the current deployment to reach the desired status
				if(!currentDeployment.getStatus().getAvailableReplicas().equals(deploy.getSpec().getReplicas())) {
					KubernetesUtils.scale(extsApi, currentDeployment.getMetadata(), deploy.getSpec().getReplicas());
					return true;
				} else {
					return true;
				}
			}
			
			deploymentToDo.setKind("Deployment");
			deploymentToDo.setApiVersion("extensions/v1beta1");

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
			deploymentToDo.setMetadata(meta);

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
				container.setStdin(Boolean.parseBoolean(c.getStdin()));
				V1SecurityContext secContext = new V1SecurityContext();
				secContext.setPrivileged(false);
				container.setSecurityContext(secContext);
				if(c.getArgs()!=null) {
					container.setArgs(c.getArgs());
				}
				containersList.add(container);
			}
			Map<String,String> nodeSelector = new HashMap<String,String>();
			nodeSelector.put("component", deploy.getTemplate().getSpec().getNodeSelector().getComponent());
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
			deploymentToDo.setSpec(spec);

			ExtensionsV1beta1Deployment result = extsApi.createNamespacedDeployment(deploy.getMetadata().getNamespace(), deploymentToDo, "true");
			io.kubernetes.client.JSON json = new io.kubernetes.client.JSON();
			String resultJson = json.serialize(result);
			System.out.println(resultJson);
			// Waits until the status is equal to the one desired
			while(!checkReplicas(extsApi, deploymentToDo.getMetadata(), deploy.getSpec().getReplicas())) {
				Thread.sleep(10000);
			}
		} catch (ApiException | IOException | InterruptedException e) {
			System.out.println("Error deploying a simulator "+deploymentToDo.getMetadata().getName());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Method used to scale a Kubernetes deployment
	 * @param extsApi 
	 * 	    reference to the extension API
	 * @param metadata
	 * 		metadata of the deployment
	 * @param replicas
	 *      number of replicas to be deployed
	 * @return true if all is OK, false otherwise
	 */
	private static boolean scale(final ExtensionsV1beta1Api extsApi, final V1ObjectMeta metadata, final Integer replicas) {
		ExtensionsV1beta1Scale body = new ExtensionsV1beta1Scale();
		body.setApiVersion("extensions/v1beta1");
		body.setKind("Scale");
		body.setMetadata(metadata);
		ExtensionsV1beta1ScaleSpec scale = new ExtensionsV1beta1ScaleSpec();
		scale.setReplicas(replicas);
		body.setSpec(scale);
		try {
			ExtensionsV1beta1Scale result = extsApi.replaceNamespacedDeploymentScale(metadata.getName(), metadata.getNamespace(), body, "true");
			io.kubernetes.client.JSON json = new io.kubernetes.client.JSON();
			String resultJson = json.serialize(result);
			System.out.println(resultJson);			
		} catch (ApiException e) {
			System.out.println("Error scaling the deployment "+metadata.getName());
			e.printStackTrace();
			return false;
		}
		// Waits until the status is equal to the one desired
		while(!checkReplicas(extsApi, metadata, replicas)) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	
	private static boolean checkReplicas(final ExtensionsV1beta1Api extsApi, final V1ObjectMeta metadata, final Integer replicas) {
		ExtensionsV1beta1Deployment currentDeployment = null; 
		try {
			currentDeployment = extsApi.readNamespacedDeploymentStatus(metadata.getName(), metadata.getNamespace(), "true");
		} catch(ApiException ex) {
			System.out.println("Exception checking the current deployment status for "+metadata.getName());
			return false;
		}
		if(currentDeployment.getStatus().getAvailableReplicas()!=null) {
			return currentDeployment.getStatus().getAvailableReplicas().equals(replicas);
		} else {
			return false;
		}
	}
}
