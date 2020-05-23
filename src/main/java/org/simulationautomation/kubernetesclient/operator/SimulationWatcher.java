package org.simulationautomation.kubernetesclient.operator;

import org.simulationautomation.kubernetesclient.crds.SimulationCR;
import org.simulationautomation.kubernetesclient.simulation.SimulationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;

@Component
public class SimulationWatcher implements Watcher<SimulationCR> {

	private Logger log = LoggerFactory.getLogger(SimulationWatcher.class);

	@Autowired
	private SimulationService simulationsService;

	@Override
	public void eventReceived(Watcher.Action action, SimulationCR simulation) {
		if (action.equals(Action.ADDED)) {
			log.info(">> Adding Simulation: " + simulation.getMetadata().getName());
			simulationsService.addSimulation(simulation.getMetadata().getName(), simulation);

		}

		if (action.equals(Action.MODIFIED)) {
			log.info(">> Modidied Simulation: " + simulation.getMetadata().getName());
			simulationsService.addSimulation(simulation.getMetadata().getName(), simulation);

		}

		if (action.equals(Action.DELETED)) {
			log.info(">> Deleting Simulation: " + simulation.getMetadata().getName());
			simulationsService.removeSimulation(simulation.getMetadata().getName());

		}

		if (simulation.getSpec() == null) {
			log.info("No Spec for resource " + simulation.getMetadata().getName());
		}
	}

	@Override
	public void onClose(KubernetesClientException cause) {

		// cause != null was due to issue like network connection loss
		if (cause != null) {
			cause.printStackTrace();
			// End pod
			System.exit(-1);
		}
	}

}
