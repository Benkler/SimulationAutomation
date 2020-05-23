package org.simulationautomation.kubernetesclient.manager;

import org.simulationautomation.kubernetesclient.api.ICustomResourceDefinitionBuilder;
import org.simulationautomation.kubernetesclient.operator.SimulationOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupManager implements ApplicationListener<ApplicationReadyEvent> {

	@Autowired
	@Qualifier("simulationCRDBuilder")
	ICustomResourceDefinitionBuilder crdb;

	@Autowired
	SimulationOperator simulationOperator;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		/*
		 * Create custom resource definition for simulation
		 */
		// crdb.createCRD();

		/*
		 * Create informer for simulation types
		 */
		// informerFactory.initInformers();

		simulationOperator.init();

	}

}
