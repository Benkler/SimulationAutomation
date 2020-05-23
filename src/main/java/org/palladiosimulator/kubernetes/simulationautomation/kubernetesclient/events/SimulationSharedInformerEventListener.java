package org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.client.informers.SharedInformerEventListener;

public class SimulationSharedInformerEventListener implements SharedInformerEventListener {

	private static final Logger log = LoggerFactory.getLogger(SimulationSharedInformerEventListener.class);

	@Override
	public void onException(Exception exception) {
		log.error("Exception caught: " + exception.getMessage());

	}
}
