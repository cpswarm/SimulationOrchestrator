package simulation.kubernetes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;

import config.deployment.Container;
import config.deployment.Deployment;
import config.deployment.Env;
import config.deployment.Port;
import config.deployment.Service;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodSecurityContext;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.SecurityContext;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
import io.fabric8.kubernetes.api.model.apps.DeploymentStrategy;
import io.fabric8.kubernetes.api.model.apps.RollingUpdateDeployment;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;


public final class KubernetesUtils {

	/**
	 * Method used to deploy a Kubernetes APP
	 * 
	 * @param deploy
	 * 		characteristics of the APP to be deployed
	 * @return true if all is OK, false otherwise
	 */
	public static boolean deploy(Deployment deploy) {
		io.fabric8.kubernetes.api.model.apps.Deployment currentDeployment = new io.fabric8.kubernetes.api.model.apps.Deployment();

		// Check the current status of the deployment
		Config config = new ConfigBuilder().build();
		KubernetesClient client = new DefaultKubernetesClient(config);
		currentDeployment = client.apps().deployments().inNamespace(deploy.getMetadata().getNamespace()).withName(deploy.getMetadata().getName()).get();
		if(currentDeployment!=null) {
			// If something is already deployed 
			// but the current number of replica is not equal to the desired one
			// it scales the current deployment to reach the desired status
			if(currentDeployment.getStatus()!=null && !currentDeployment.getStatus().getAvailableReplicas().equals(deploy.getSpec().getReplicas())) {
				KubernetesUtils.scale(client, currentDeployment.getMetadata(), deploy.getSpec().getReplicas());
			} 
			return true;
		}
		Map<String,String> labelsMap = new HashMap<String,String>();
		labelsMap.put("k8s-app", deploy.getMetadata().getLabels().getK8sApp());
		Map<String,String> annotationsMap = new HashMap<String,String>();
		annotationsMap.put("deployment.kubernetes.io/revision", "1");
		Map<String,String> matchLabelsMap = new HashMap<String,String>();
		matchLabelsMap.put("k8s-app", deploy.getSpec().getSelector().getMatchLabels().getK8sApp());
		LabelSelector labelSelector = new LabelSelector();
		labelSelector.setMatchLabels(matchLabelsMap);

		io.fabric8.kubernetes.api.model.apps.Deployment deploymentToDo = new io.fabric8.kubernetes.api.model.apps.Deployment();
		deploymentToDo.setKind("Deployment");
		deploymentToDo.setApiVersion("extensions/v1beta1");
		ObjectMeta metadata = new ObjectMeta();
		metadata.setName(deploy.getMetadata().getName());
		metadata.setNamespace(deploy.getMetadata().getNamespace());
		metadata.setSelfLink("/apis/extensions/v1beta1/namespaces/default/deployments/"+deploy.getMetadata().getName());
		metadata.setUid(UUID.randomUUID().toString());
		metadata.setGeneration(Long.valueOf(1));
		metadata.setLabels(labelsMap);
		deploymentToDo.setMetadata(metadata);
		DeploymentSpec spec = new DeploymentSpec();
		spec.setReplicas(deploy.getSpec().getReplicas());
		spec.setSelector(labelSelector);
		PodTemplateSpec template = new PodTemplateSpec();
		ObjectMeta meta = new ObjectMeta();
		meta.setName(deploy.getTemplate().getMetadata().getName());
		meta.setLabels(labelsMap);
		template.setMetadata(meta);
		PodSpec podSpec = new PodSpec();
		List<io.fabric8.kubernetes.api.model.Container> containersList = new ArrayList<io.fabric8.kubernetes.api.model.Container>();
		for (Container c : deploy.getTemplate().getSpec().getContainers()) {
			io.fabric8.kubernetes.api.model.Container container = new io.fabric8.kubernetes.api.model.Container();
			container.setName(c.getName());
			container.setImage(c.getImage());
			container.setTerminationMessagePath("/dev/termination-log");
			container.setTerminationMessagePolicy("File");
			container.setImagePullPolicy("IfNotPresent");
			container.setStdin(Boolean.parseBoolean(c.getStdin()));
			SecurityContext secContext = new SecurityContext();
			secContext.setPrivileged(false);
			container.setSecurityContext(secContext);
			if(c.getArgs()!=null) {
				container.setArgs(c.getArgs());
			}
			if(c.getEnv()!=null) {
				for (Env env : c.getEnv()) {
					EnvVar var = new EnvVar();
					var.setName(env.getName());
					var.setValue(env.getValue());
					container.getEnv().add(var);
				}
			}
			if(c.getResources()!=null) {
				ResourceRequirements resources = new ResourceRequirements();
				if(c.getResources().getRequests()!=null) {
					Map<String, Quantity> requests = new HashMap<String, Quantity>();
					if(c.getResources().getRequests().getCpu()!=null) {
						Quantity quantity = new Quantity();
						quantity.setAmount(c.getResources().getRequests().getCpu().toString());
						requests.put("cpu", quantity);
					}
					if(c.getResources().getRequests().getMemory()!=null) {
						Quantity quantity = new Quantity();
						quantity.setAmount(c.getResources().getRequests().getMemory().toString());
						requests.put("memory", quantity);
					}
					resources.setRequests(requests);
				}
				if(c.getResources().getLimits()!=null) {
					Map<String, Quantity> limits = new HashMap<String, Quantity>();
					if(c.getResources().getLimits().getCpu()!=null) {
						Quantity quantity = new Quantity();
						quantity.setAmount(c.getResources().getLimits().getCpu().toString());
						limits.put("cpu", quantity);
					}
					if(c.getResources().getLimits().getMemory()!=null) {
						Quantity quantity = new Quantity();
						quantity.setAmount(c.getResources().getLimits().getMemory().toString());
						limits.put("memory", quantity);
					}
					resources.setLimits(limits);
				}
				container.setResources(resources);
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
		PodSecurityContext podSecContext = new PodSecurityContext();
		podSpec.setSecurityContext(podSecContext);
		podSpec.setSchedulerName("default-scheduler");
		template.setSpec(podSpec);
		DeploymentStrategy strategy = new DeploymentStrategy();
		strategy.setType("RollingUpdate");
		RollingUpdateDeployment rollingUpdate = new RollingUpdateDeployment();
		rollingUpdate.setMaxUnavailable(new IntOrString("25%"));
		rollingUpdate.setMaxSurge(new IntOrString("25%"));
		strategy.setRollingUpdate(rollingUpdate);
		spec.setTemplate(template);
		spec.setStrategy(strategy);
		spec.setRevisionHistoryLimit(10);
		spec.setProgressDeadlineSeconds(600);
		deploymentToDo.setSpec(spec);
		Gson gson = new Gson();
		String deploymentJson = gson.toJson(deploymentToDo, io.fabric8.kubernetes.api.model.apps.Deployment.class);
		System.out.println(deploymentJson);
		
		
		io.fabric8.kubernetes.api.model.apps.Deployment result = client.apps().deployments().inNamespace(deploy.getMetadata().getNamespace()).create(deploymentToDo);
		String resultJson = gson.toJson(result, io.fabric8.kubernetes.api.model.apps.Deployment.class);
		System.out.println(resultJson);
		// Waits until the status is equal to the one desired
		while(!checkReplicas(client, deploymentToDo.getMetadata(), deploy.getSpec().getReplicas())) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}


	/**
	 * Method used to deploy a Kubernetes service
	 * 
	 * @param service
	 * 		characteristics of the service
	 * @return true if all is OK, false otherwise
	 */
	public static boolean installService(Service service) {
		io.fabric8.kubernetes.api.model.Service currentService = null;
		io.fabric8.kubernetes.api.model.Service serviceToDo = new io.fabric8.kubernetes.api.model.Service (); 
		// Check the current status of the service
		Config config = new ConfigBuilder().build();
		KubernetesClient client = new DefaultKubernetesClient(config);
		currentService = client.services().inNamespace(service.getMetadata().getNamespace()).withName(service.getMetadata().getName()).get();
		if(currentService!=null) {
			client.close();
			return true;
		} else {
			Map<String,String> labelsMap = new HashMap<String,String>();
			labelsMap.put("application", service.getMetadata().getApplication());
			ObjectMeta metadata = new ObjectMeta();
			metadata.setName(service.getMetadata().getName());
			metadata.setNamespace(service.getMetadata().getNamespace());
			metadata.setLabels(labelsMap);
			serviceToDo.setMetadata(metadata);
			ServiceSpec serviceSpec = new ServiceSpec();
			List<ServicePort> ports = new ArrayList<ServicePort>();
			for(final Port port : service.getSpec().getPorts()) {
				ServicePort servicePort = new ServicePort();
				servicePort.setName(port.getName());
				servicePort.setPort(port.getPort());
				servicePort.setProtocol(port.getProtocol());
				servicePort.setTargetPort(new IntOrString(port.getTargetPort()));
				servicePort.setNodePort(port.getNodePort());
				ports.add(servicePort);
			}
			serviceSpec.setPorts(ports);
			Map<String,String> selectorMap = new HashMap<String,String>();
			selectorMap.put("k8s-app", service.getSelector().getApplication());
			serviceSpec.setSelector(selectorMap);
			serviceSpec.setType(service.getType());
			serviceToDo.setSpec(serviceSpec);
		}
		client.services().inNamespace(service.getMetadata().getNamespace()).create(serviceToDo);
		client.close();
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
	private static boolean scale(final KubernetesClient client, final ObjectMeta metadata, final Integer replicas) {
		io.fabric8.kubernetes.api.model.apps.Deployment result = client.apps().deployments().inNamespace(metadata.getNamespace()).withName(metadata.getName()).scale(replicas, true); 
		Gson gson = new Gson();
		String deploymentJson = gson.toJson(result, io.fabric8.kubernetes.api.model.apps.Deployment.class);
		System.out.println(deploymentJson);
		// Waits until the status is equal to the one desired
		while(!checkReplicas(client, metadata, replicas)) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}


	private static boolean checkReplicas(final KubernetesClient client, final ObjectMeta metadata, final Integer replicas) {
		io.fabric8.kubernetes.api.model.apps.Deployment currentDeployment = client.apps().deployments().inNamespace(metadata.getNamespace()).withName(metadata.getName()).get(); 
		if(currentDeployment.getStatus() != null && currentDeployment.getStatus().getAvailableReplicas()!=null) {
			return currentDeployment.getStatus().getAvailableReplicas().equals(replicas);
		} else {
			return false;
		}
	}
}
