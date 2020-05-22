package org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

@Component
public class CustomResourceDefinitionBuilder implements ApplicationListener<ApplicationReadyEvent> {

	private static final Logger log = LoggerFactory.getLogger(CustomResourceDefinitionBuilder.class);

	private CustomResourceDefinitionContext simulationCrdContext;

	@Autowired
	KubernetesClient client;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {

		// Load CRD as object from YAML
		CustomResourceDefinition animalCrd = client.customResourceDefinitions()
				.load(CustomResourceDefinitionBuilder.class.getResourceAsStream("/simulation-crd.yml")).get();
		// Apply CRD object onto your Kubernetes cluster

		client.customResourceDefinitions().createOrReplace(animalCrd);
	}

	public CustomResourceDefinitionContext getCRDContext() {
		if (simulationCrdContext == null) {
			simulationCrdContext = new CustomResourceDefinitionContext.Builder().withGroup("palladio.org")
					.withScope("Namespaced").withVersion("v1").withPlural("simulators").build();
		}
		return simulationCrdContext;

	}

}
