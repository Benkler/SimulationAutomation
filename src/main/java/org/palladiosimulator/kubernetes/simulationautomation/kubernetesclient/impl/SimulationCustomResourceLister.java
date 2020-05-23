package org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.impl;

import org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.api.ICustomResourceDefinitionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import io.fabric8.kubernetes.client.KubernetesClient;

public class SimulationCustomResourceLister {

	@Autowired
	@Qualifier("simulationCRDBuilder")
	ICustomResourceDefinitionBuilder crdBuilder;

	@Autowired
	KubernetesClient client;

	public void listCustomResources(String namespace) {

		client.customResource(crdBuilder.getCRDContext()).list(namespace);

	}

}
