package org.simulationautomation.kubernetesclient.manager;

import org.simulationautomation.kubernetesclient.operator.SimulationOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupManager implements ApplicationListener<ApplicationReadyEvent> {

	@Autowired
	SimulationOperator simulationOperator;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {

		simulationOperator.init();

	}

}
