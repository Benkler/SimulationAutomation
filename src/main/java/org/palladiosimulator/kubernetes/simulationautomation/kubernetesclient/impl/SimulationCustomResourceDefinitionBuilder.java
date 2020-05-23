package org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.impl;

import org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.api.ICustomResourceDefinitionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

/**
 * Build Simulation CRD on startup and provided CRD context.
 * 
 * @author Niko Benkler
 *
 */
@Component(value = "simulationCRDBuilder")
public class SimulationCustomResourceDefinitionBuilder
		implements ApplicationListener<ApplicationReadyEvent>, ICustomResourceDefinitionBuilder {

	private static final Logger log = LoggerFactory.getLogger(SimulationCustomResourceDefinitionBuilder.class);

	private CustomResourceDefinitionContext simulationCrdContext;

	@Autowired
	KubernetesClient client;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		createCRD();

	}

	@Override
	public void createCRD() {
		// Load CRD as object from YAML
		CustomResourceDefinition crd = client.customResourceDefinitions()
				.load(SimulationCustomResourceDefinitionBuilder.class.getResourceAsStream("/simulation-crd.yml")).get();
		// Apply CRD object onto your Kubernetes cluster

		client.customResourceDefinitions().createOrReplace(crd);
		log.info("Custom resource definition successfully created");
	}

	@Override
	public CustomResourceDefinitionContext getCRDContext() {
		if (simulationCrdContext == null) {
			simulationCrdContext = new CustomResourceDefinitionContext.Builder().withGroup("palladio.org")
					.withScope("Namespaced").withVersion("v1").withPlural("simulators").build();
		}
		return simulationCrdContext;

	}

}
