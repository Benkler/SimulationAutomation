package org.simulationautomation.kubernetesclient.operator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.fabric8.kubernetes.api.model.Doneable;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;

@Service
public class K8SCoreRuntime {

	private Logger logger = LoggerFactory.getLogger(K8SCoreRuntime.class);

	@Autowired
	private KubernetesClient kubernetesClient;

	public void registerCustomKind(String apiVersion, String kind,
			Class<? extends KubernetesResource> clazz) {
		KubernetesDeserializer.registerCustomKind(apiVersion, kind, clazz);
	}

	public CustomResourceDefinitionList getCustomResourceDefinitionList() {
		return kubernetesClient.customResourceDefinitions()
			.list();
	}

	public <T extends HasMetadata, L extends KubernetesResourceList<?>, D extends Doneable<T>> MixedOperation<T, L, D, Resource<T, D>> customResourcesClient(
			CustomResourceDefinition crd, Class<T> resourceType,
			Class<L> listClass, Class<D> doneClass) {
		return kubernetesClient.customResources(crd, resourceType, listClass,
				doneClass);
	}

	public <T extends HasMetadata, L extends KubernetesResourceList<?>, D extends Doneable<T>> NonNamespaceOperation<T, L, D, Resource<T, D>> customResourcesClientInNameSpace(
			CustomResourceDefinition crd, Class<T> resourceType,
			Class<L> listClass, Class<D> doneClass, String namespace) {
		return kubernetesClient
			.customResources(crd, resourceType, listClass, doneClass)
			.inNamespace(namespace);
	}

	public void registerCustomResourceDefinition(CustomResourceDefinition crd) {
		// Apply CRD object onto your Kubernetes cluster
		kubernetesClient.customResourceDefinitions()
			.createOrReplace(crd);
	}

}
