package org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.impl;

import java.util.List;

import org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.api.ICustomResourceDefinitionBuilder;
import org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.crd.SimulationCR;
import org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.crd.SimulationCRDoneable;
import org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.crd.SimulationCRList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

@Component
public class SimulationCustomResourceLister {

	@Autowired
	@Qualifier("simulationCRDBuilder")
	ICustomResourceDefinitionBuilder crdBuilder;

	@Autowired
	KubernetesClient client;

	/**
	 * List custom Resources for type Simulation in given namepsace
	 * 
	 * @param namespace
	 * @return
	 */
	public List<SimulationCR> listCustomResources(String namespace) {

		NonNamespaceOperation<SimulationCR, SimulationCRList, SimulationCRDoneable, Resource<SimulationCR, SimulationCRDoneable>> custClient = client
				.customResources(crdBuilder.getCRD(), SimulationCR.class, SimulationCRList.class,
						SimulationCRDoneable.class)
				.inNamespace(namespace);

		return custClient.list().getItems();

	}

}
