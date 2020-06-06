package org.simulationautomation.kubernetesclient.manager;

import static org.simulationautomation.kubernetesclient.simulation.SimulationCRDs.SIMULATION_NAMESPACE;

import org.simulationautomation.kubernetesclient.nfs.NFSCreator;
import org.simulationautomation.kubernetesclient.operator.SimulationOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.client.KubernetesClient;
@Component
public class ApplicationStartupManager
		implements
			ApplicationListener<ApplicationReadyEvent> {

	@Autowired
	SimulationOperator simulationOperator;

	@Autowired
	NFSCreator nfsCreator;

	@Autowired
	KubernetesClient client;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {

		clearCluster();
		nfsCreator.createNFS(SIMULATION_NAMESPACE);
		simulationOperator.init();

	}

	private void clearCluster() {
		// Remove old pods
		client.pods()
			.inNamespace(SIMULATION_NAMESPACE)
			.delete();

	}

}
