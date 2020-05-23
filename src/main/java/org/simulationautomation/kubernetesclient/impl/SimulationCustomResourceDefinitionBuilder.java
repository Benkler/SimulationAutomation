package org.simulationautomation.kubernetesclient.impl;

import org.simulationautomation.kubernetesclient.api.ICustomResourceDefinitionBuilder;
import org.simulationautomation.kubernetesclient.crds.SimulationCR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;

/**
 * Build Simulation CRD on startup and provided CRD context.
 * 
 * @author Niko Benkler
 *
 */
@Component(value = "simulationCRDBuilder")
public class SimulationCustomResourceDefinitionBuilder implements ICustomResourceDefinitionBuilder {

	private static final Logger log = LoggerFactory.getLogger(SimulationCustomResourceDefinitionBuilder.class);

	private static final String PATH_TO_SIMULATION_CRD = "/simulation-crd.yml";
	private static final String SIMULATION_GROUP = "palladio.org";
	private static final String VERSION = "v1";
	private static final String SIMULATION_NAME = "Simulation";

	private CustomResourceDefinitionContext simulationCrdContext;

	private CustomResourceDefinition simulationCrd;

	@Autowired
	KubernetesClient client;

	/**
	 * Create Custom Resource Definition for Kind=Simulation and register
	 */

	private void createCRD() {
		// Load CRD as object from YAML
		CustomResourceDefinition crd = client.customResourceDefinitions()
				.load(SimulationCustomResourceDefinitionBuilder.class.getResourceAsStream(PATH_TO_SIMULATION_CRD))
				.get();

		log.info("Custom resource definition successfully created");
		this.simulationCrd = crd;
	}

	/**
	 * Get custom resource definition for kind=simulation
	 */
	@Override
	public CustomResourceDefinition getCRD() {
		if (simulationCrd == null) {
			createCRD();
		}
		return simulationCrd;
	}

	@Override
	public CustomResourceDefinitionContext getCRDContext() {
		if (simulationCrdContext == null) {
			simulationCrdContext = new CustomResourceDefinitionContext.Builder().withGroup(SIMULATION_GROUP)
					.withScope("Namespaced").withVersion(VERSION).withPlural("simulators").build();
		}
		return simulationCrdContext;

	}

	private void registerSimulationCR() {
		log.info("Register SimulationCR");
		KubernetesDeserializer.registerCustomKind(SIMULATION_GROUP + "/" + VERSION, SIMULATION_NAME,
				SimulationCR.class);

	}

}
