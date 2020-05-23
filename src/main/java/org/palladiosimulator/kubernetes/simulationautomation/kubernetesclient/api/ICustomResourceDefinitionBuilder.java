package org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.api;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

public interface ICustomResourceDefinitionBuilder {

	void createCRD();

	CustomResourceDefinitionContext getCRDContext();

	CustomResourceDefinition getCRD();

}