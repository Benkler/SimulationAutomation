package org.simulationautomation.kubernetesclient.operator;

import static org.simulationautomation.kubernetesclient.simulation.SimulationCRDs.SIMULATION_CRD_KIND;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.simulationautomation.kubernetesclient.crds.SimulationCR;
import org.simulationautomation.kubernetesclient.simulation.SimulationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;

@Component
public class SimulationPodWatcher implements Watcher<Pod> {

	private Logger log = LoggerFactory.getLogger(SimulationPodWatcher.class);

	@Autowired
	private SimulationService simulationsService;

	private Set<String> resourceVersions = new HashSet<>();

	@Override
	public void eventReceived(Watcher.Action action, Pod pod) {
		if (alreadyReceived(pod)) {
			return;
		}

		OwnerReference ownerReference = getControllerOf(pod);

		if (ownerReference == null || !ownerReference.getKind().equalsIgnoreCase(SIMULATION_CRD_KIND)) {
			return;
		}

		if (action.equals(Action.ADDED)) {
			log.info("Handle Add Pod: " + pod.getMetadata().getName());
			SimulationCR simulation = getSimulationFromPod(pod);

		}

		if (action.equals(Action.MODIFIED)) {

			log.info("Handle Modify Pod: " + pod.getMetadata().getName());
			SimulationCR simulation = getSimulationFromPod(pod);
			// simulationsService.addSimulation(pod.getMetadata().getName(), pod);

		}

		if (action.equals(Action.DELETED)) {
			log.info("Handle Delete Pod: " + pod.getMetadata().getName());

		}

		if (pod.getSpec() == null) {
			log.info("No Spec for resource " + pod.getMetadata().getName());
		}
	}

	private SimulationCR getSimulationFromPod(Pod pod) {
		OwnerReference ownerReference = getControllerOf(pod);

		SimulationCR simulation = simulationsService.getSimulation(ownerReference.getName());
		if (simulation != null) {
			log.info("Simulation found for pod with name: " + pod.getMetadata().getName());

		} else {
			log.info("Simulation not found for pod with name: " + pod.getMetadata().getName());
		}

		return simulation;
	}

	@Override
	public void onClose(KubernetesClientException cause) {

		log.info("Pod closed");
		// cause != null was due to issue like network connection loss
		if (cause != null) {
			cause.printStackTrace();
			// End pod
			System.exit(-1);
		}
	}

	/*
	 * Pod has a SimulationCR Owner
	 */
	private OwnerReference getControllerOf(Pod pod) {
		List<OwnerReference> ownerReferences = pod.getMetadata().getOwnerReferences();
		for (OwnerReference ownerReference : ownerReferences) {
			if (ownerReference.getController().equals(Boolean.TRUE)) {
				return ownerReference;
			}
		}
		return null;
	}

	/*
	 * Filter event, which were already received
	 */
	private boolean alreadyReceived(Pod pod) {

		String podAndResourceVersion = pod.getMetadata().getUid() + pod.getMetadata().getResourceVersion();
		if (resourceVersions.contains(podAndResourceVersion)) {
			return true;
		} else {
			resourceVersions.add(podAndResourceVersion);
			return false;
		}

	}

}
