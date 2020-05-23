package org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.impl;

import java.util.concurrent.Executors;

import org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.api.ICustomResourceDefinitionBuilder;
import org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.api.IInformerFactory;
import org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.crd.SimulationCR;
import org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.crd.SimulationCRList;
import org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.events.SimulationResourceEventHandler;
import org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.events.SimulationSharedInformerEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;

@Component(value = "simulationInformerFactory")
public class SimulationInformerFactory implements IInformerFactory {

	@Autowired
	KubernetesClient client;

	@Autowired
	ICustomResourceDefinitionBuilder crdBuilder;

	private static final Logger log = LoggerFactory.getLogger(SimulationCustomResourceBuilder.class);

	/**
	 * Add EventHandler for SimulationCR.class
	 */
	@Override
	public void initInformers() {
		log.info("Create informer factory");
		SharedInformerFactory informerFactory = client.informers();

		SharedIndexInformer<SimulationCR> simulationPodInformer = informerFactory.sharedIndexInformerForCustomResource(
				crdBuilder.getCRDContext(), SimulationCR.class, SimulationCRList.class, 1 * 60 * 1000);

		log.info("Add Event Handler for Simulation CR");
		simulationPodInformer.addEventHandler(new SimulationResourceEventHandler());

		informerFactory.addSharedInformerEventListener(new SimulationSharedInformerEventListener());

		log.info("Starting all registered informers for SimulationCR");
		informerFactory.startAllRegisteredInformers();

		Executors.newSingleThreadExecutor().submit(() -> {
			Thread.currentThread().setName("HAS_SYNCED_THREAD");
			try {

				while (!simulationPodInformer.hasSynced()) {
					log.info("podInformer.hasSynced() : {}", simulationPodInformer.hasSynced());
					Thread.sleep(200);
				}

			} catch (InterruptedException inEx) {
				log.info("HAS_SYNCED_THREAD INTERRUPTED!");
			}
		});

	}

}
