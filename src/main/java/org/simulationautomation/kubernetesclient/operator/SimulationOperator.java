package org.simulationautomation.kubernetesclient.operator;

import static org.simulationautomation.kubernetesclient.simulation.SimulationCRDs.SIMULATION_CRD_GROUP;
import static org.simulationautomation.kubernetesclient.simulation.SimulationCRDs.SIMULATION_CRD_KIND;
import static org.simulationautomation.kubernetesclient.simulation.SimulationCRDs.SIMULATION_CRD_NAME;
import static org.simulationautomation.kubernetesclient.simulation.SimulationCRDs.SIMULATION_NAMESPACE;

import java.util.Collections;
import java.util.List;

import org.simulationautomation.kubernetesclient.api.ICustomResourceBuilder;
import org.simulationautomation.kubernetesclient.api.ICustomResourceDefinitionBuilder;
import org.simulationautomation.kubernetesclient.crds.SimulationCR;
import org.simulationautomation.kubernetesclient.crds.SimulationCRDoneable;
import org.simulationautomation.kubernetesclient.crds.SimulationCRList;
import org.simulationautomation.kubernetesclient.simulation.SimulationService;
import org.simulationautomation.kubernetesclient.util.CustomNamespaceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.builder.Predicate;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

@Component
public class SimulationOperator {

	static final String IMAGE_PULL_POLICY = "IfNotPresent";
	static final String POD_RESTART_POLICY = "Never";
	static final String POD_PHASE_SUCCEEDED = "Succeeded";
	static final String POD_PHASE_FAILED = "Failed";
	private static final JobFinishedPredicate POD_PREDICATE = new JobFinishedPredicate();

	private Logger log = LoggerFactory.getLogger(SimulationOperator.class);

	private CustomResourceDefinition simulationCRD = null;

	private String simulationResourceVersion;

	private NonNamespaceOperation<SimulationCR, SimulationCRList, SimulationCRDoneable, Resource<SimulationCR, SimulationCRDoneable>> simulationCRDClient;

	@Autowired
	private KubernetesClient client;

	@Autowired
	private SimulationService simulationsService;

	@Autowired
	private SimulationWatcher simulationWatcher;

	@Autowired
	private SimulationPodWatcher simulationPodWatcher;

	@Autowired
	private K8SCoreRuntime k8SCoreRuntime;

	@Autowired
	CustomNamespaceBuilder nsBuilder;

	@Autowired
	ICustomResourceDefinitionBuilder crdBuilder;

	@Autowired
	ICustomResourceBuilder customResourceBuilder;

	/*
	 * Init can only be called if all the required CRDs are present - It creates
	 * the CRD clients to be able to watch and execute operations - It loads the
	 * existing resources (current state in the cluster) - It register the
	 * watches for our CRDs
	 */
	public void init() {
		nsBuilder.createNamespace(SIMULATION_NAMESPACE);
		registerSimulationCRD();
		// Creating CRD Client for simulation
		simulationCRDClient = k8SCoreRuntime.customResourcesClient(
				simulationCRD, SimulationCR.class, SimulationCRList.class,
				SimulationCRDoneable.class);
		// Load existing custom resources
		deleteExistingSimulationsResources();
		registerSimulationWatch();

	}

	/**
	 * List custom Resources for type Simulation in given namepsace
	 * 
	 * @param namespace
	 * @return
	 * @return
	 */
	public List<SimulationCR> listExistingSimulations() {
		return simulationCRDClient.list()
			.getItems();
	}

	private void registerSimulationCRD() {

		simulationCRD = crdBuilder.getCRD();
		k8SCoreRuntime.registerCustomResourceDefinition(simulationCRD);
		k8SCoreRuntime.registerCustomKind(SIMULATION_CRD_GROUP + "/v1",
				SIMULATION_CRD_KIND, SimulationCR.class);

		log.info("SimulationCRD successfully registered");
	}

	/*
	 * Delete existing resources after startup to avoid clashes in resource
	 * version
	 */
	private void deleteExistingSimulationsResources() {
		// Load Existing Simulations
		List<SimulationCR> simulationList = simulationCRDClient.list()
			.getItems();

		simulationCRDClient.delete();

		// for(SimulationCR simulation : simulationList) {
		//
		// }
		//
		// if (!simulationList.isEmpty()) {
		//
		// simulationList.forEach(simulation -> {
		// simulationsService.addSimulation(simulation.getMetadata()
		// .getName(), simulation);
		// log.info("Simulation " + simulation.getMetadata()
		// .getName() + " found. Add to SimulationService.");
		// });
		//
		// }

	}

	/*
	 * Register Simulation Watch - This watcher is in charge of SimulationCRs
	 */
	private void registerSimulationWatch() {
		log.info("Registering CRD Watch");
		// TODO was ist ResourceVersion
		// simulationCRDClient.withResourceVersion(simulationResourceVersion)
		simulationCRDClient.watch(simulationWatcher);
		// client.pods().withResourceVersion(simulationResourceVersion).watch(simulationPodWatcher);
		log.info("Registering Pod Watch");
		client.pods()
			.watch(simulationPodWatcher);

	}

	/*
	 * Check that all the CRDs are found for this operator to work
	 */
	private boolean allCRDsFound() {
		if (simulationCRD == null) {
			return false;
		}
		return true;
	}

	/**
	 * Create a simulation custom resource with given name in simulation
	 * namespace. </br>
	 * 
	 * @param name
	 */
	public void createSimulation(String name) {
		log.info("Trying to create simulation with name=" + name);
		customResourceBuilder.createCustomResource(name, SIMULATION_NAMESPACE);
		// Busy waiting
		// Future?
		while (simulationsService.getSimulation(name) == null);

		SimulationCR simulation = simulationsService.getSimulation(name);
		addSimulationPod(simulation);
		log.info("Successfully added simulation with name=" + name);

	}

	/**
	 * 
	 * @param simulation
	 */
	private void addSimulationPod(SimulationCR simulation) {
		log.info("Trying to add pod with name=" + simulation.getMetadata()
			.getName() + " in namespace=" + SIMULATION_NAMESPACE);
		Pod pod = createNewPod(simulation);
		client.pods()
			.inNamespace(SIMULATION_NAMESPACE)
			.create(pod);

	}

	private Pod createNewPod(SimulationCR simulation) {

		Container container = createSimulationContainer(simulation);

		return new PodBuilder().withNewMetadata()
			.withGenerateName(simulation.getMetadata()
				.getName() + "-pod")
			.withNamespace(simulation.getMetadata()
				.getNamespace())
			.withLabels(Collections.singletonMap("app", simulation.getMetadata()
				.getName()))
			.addNewOwnerReference()
			.withController(true)
			.withKind(SIMULATION_CRD_KIND)
			.withApiVersion("demo.k8s.io/v1alpha1")
			.withName(simulation.getMetadata()
				.getName())
			.withNewUid(simulation.getMetadata()
				.getUid())
			.endOwnerReference()
			.endMetadata()
			.withNewSpec()
			.withContainers(container)
			.withRestartPolicy(POD_RESTART_POLICY)
			.endSpec()
			.build();
	}

	private Container createSimulationContainer(SimulationCR simulation) {

		return new ContainerBuilder().withName("palladiosumlation")
			.withImage("palladiosimulator/eclipse")
			.withImagePullPolicy(IMAGE_PULL_POLICY)
			.build();
	}

	/*
	 * Check for Required CRDs
	 */
	private boolean areRequiredCRDsPresent() {
		try {

			k8SCoreRuntime.registerCustomKind(SIMULATION_CRD_GROUP + "/v1",
					SIMULATION_CRD_KIND, SimulationCR.class);

			CustomResourceDefinitionList crds = k8SCoreRuntime
				.getCustomResourceDefinitionList();
			for (CustomResourceDefinition crd : crds.getItems()) {
				ObjectMeta metadata = crd.getMetadata();
				if (metadata != null) {
					String name = metadata.getName();

					if (SIMULATION_CRD_NAME.equals(name)) {
						simulationCRD = crd;
					}
				}
			}
			if (allCRDsFound()) {
				return true;
			} else {
				log.error("Custom CRDs not available");

				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("> Init sequence not done");
		}
		return false;
	}

	/**
	 * Checks whether pod is Failed or Successfully finished command execution
	 */
	static class JobFinishedPredicate implements Predicate<Pod> {
		@Override
		public Boolean apply(Pod pod) {
			if (pod.getStatus() == null) {
				return false;
			}
			switch (pod.getStatus()
				.getPhase()) {
				case POD_PHASE_FAILED :
					// fall through
				case POD_PHASE_SUCCEEDED :
					// job is finished.
					return true;
				default :
					// job is not finished.
					return false;
			}
		}
	}

	// void execute(String workspaceId, String[] commandBase, String...
	// arguments) {
	// final String jobName = commandBase[0];
	// final String podName = jobName + '-' + workspaceId;
	// final String[] command = buildCommand(commandBase, arguments);
	// final Pod pod = newPod(podName, command);
	// OpenShiftPods pods = null;
	// try {
	// pods = factory.create(workspaceId).pods();
	// pods.create(pod);
	// final Pod finished = pods.wait(podName, WAIT_POD_TIMEOUT_MIN,
	// POD_PREDICATE::apply);
	// if (POD_PHASE_FAILED.equals(finished.getStatus().getPhase())) {
	// LOG.error("Job command '%s' execution is failed.",
	// Arrays.toString(command));
	// }
	// } catch (InfrastructureException ex) {
	// LOG.error(
	// "Unable to perform '{}' command for the workspace '{}' cause: '{}'",
	// Arrays.toString(command),
	// workspaceId,
	// ex.getMessage());
	// } finally {
	// if (pods != null) {
	// try {
	// pods.delete(podName);
	// } catch (InfrastructureException ignored) {
	// }
	// }
	// }
	// }

}
