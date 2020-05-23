package org.simulationautomation.kubernetesclient.api;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

public interface ICustomResourceDefinitionBuilder {

	CustomResourceDefinitionContext getCRDContext();

	CustomResourceDefinition getCRD();

}