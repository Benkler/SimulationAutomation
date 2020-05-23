package org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.events;

import org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.crd.SimulationCR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.client.informers.ResourceEventHandler;

public class SimulationResourceEventHandler implements ResourceEventHandler<SimulationCR> {

	private static final Logger log = LoggerFactory.getLogger(SimulationResourceEventHandler.class);

	@Override
	public void onUpdate(SimulationCR oldObj, SimulationCR newObj) {
		if (oldObj.getMetadata().getResourceVersion() == newObj.getMetadata().getResourceVersion()) {
			return;
		}

		log.info("update name " + newObj.getMetadata().getName());

	}

	@Override
	public void onDelete(SimulationCR obj, boolean deletedFinalStateUnknown) {
		log.info("delete");

	}

	@Override
	public void onAdd(SimulationCR obj) {
		log.info("add with name=" + obj.getMetadata().getName());
	}

}
