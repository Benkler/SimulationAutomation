package org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.old;

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
public class SimulationCustomClientFactory {

	@Autowired
	KubernetesClient client;

	@Autowired
	@Qualifier("simulationCRDBuilder")
	ICustomResourceDefinitionBuilder crdb;

	public List<SimulationCR> bla() {

		NonNamespaceOperation<SimulationCR, SimulationCRList, SimulationCRDoneable, Resource<SimulationCR, SimulationCRDoneable>> custClient = client
				.customResources(crdb.getCRD(), SimulationCR.class, SimulationCRList.class, SimulationCRDoneable.class)
				.inNamespace("otherspace");

		return custClient.list().getItems();

	}

}
