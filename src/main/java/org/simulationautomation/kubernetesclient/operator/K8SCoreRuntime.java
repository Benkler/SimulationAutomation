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

/**
 * Service Class for k8s Core functionality
 * 
 * @author Niko Benkler
 *
 */
@Service
public class K8SCoreRuntime {

  private Logger log = LoggerFactory.getLogger(K8SCoreRuntime.class);

  @Autowired
  private KubernetesClient kubernetesClient;

  /**
   * Register custom kind at the @KubernetesDeserializer </br>
   * Necessary, as framework has to be able to deserialize crd
   * 
   * @param apiVersion
   * @param kind
   * @param clazz
   */
  public void registerCustomKind(String apiVersion, String kind,
      Class<? extends KubernetesResource> clazz) {
    KubernetesDeserializer.registerCustomKind(apiVersion, kind, clazz);
    log.info("Successfully registered custom kind with kind= " + kind);
  }

  /**
   * Retrieve currently registered custom resource definitions
   * 
   * @return
   */
  public CustomResourceDefinitionList getCustomResourceDefinitionList() {
    return kubernetesClient.customResourceDefinitions().list();
  }

  /**
   * Retrieve custom resource client for given crd type in default namespace </br>
   * Necessary in order to perform queries to the k8s cluster with crd
   * 
   * @param <T>
   * @param <L>
   * @param <D>
   * @param crd
   * @param resourceType
   * @param listClass
   * @param doneClass
   * @return
   */
  public <T extends HasMetadata, L extends KubernetesResourceList<?>, D extends Doneable<T>> MixedOperation<T, L, D, Resource<T, D>> customResourcesClient(
      CustomResourceDefinition crd, Class<T> resourceType, Class<L> listClass, Class<D> doneClass) {
    return kubernetesClient.customResources(crd, resourceType, listClass, doneClass);
  }

  /**
   * Retrieve custom resource client for given crd type in give namespace </br>
   * Necessary in order to perform queries to the k8s cluster with crd
   * 
   * @param <T>
   * @param <L>
   * @param <D>
   * @param crd
   * @param resourceType
   * @param listClass
   * @param doneClass
   * @param namespace
   * @return
   */
  public <T extends HasMetadata, L extends KubernetesResourceList<?>, D extends Doneable<T>> NonNamespaceOperation<T, L, D, Resource<T, D>> customResourcesClientInNameSpace(
      CustomResourceDefinition crd, Class<T> resourceType, Class<L> listClass, Class<D> doneClass,
      String namespace) {
    return kubernetesClient.customResources(crd, resourceType, listClass, doneClass)
        .inNamespace(namespace);
  }

  /**
   * Register custom resource definition to kubenetes cluster
   * 
   * @param crd
   */
  public void registerCustomResourceDefinition(CustomResourceDefinition crd) {
    // Apply CRD object onto your Kubernetes cluster
    kubernetesClient.customResourceDefinitions().createOrReplace(crd);
  }

}
