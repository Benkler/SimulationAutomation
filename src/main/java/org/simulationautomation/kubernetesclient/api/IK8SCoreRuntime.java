package org.simulationautomation.kubernetesclient.api;

import io.fabric8.kubernetes.api.model.Doneable;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;


public interface IK8SCoreRuntime {

  /**
   * Register custom kind at the @KubernetesDeserializer </br>
   * Necessary, as framework has to be able to deserialize crd
   * 
   * @param apiVersion
   * @param kind
   * @param clazz
   */
  void registerCustomKind(String apiVersion, String kind,
      Class<? extends KubernetesResource> clazz);

  /**
   * Retrieve currently registered custom resource definitions
   * 
   * @return
   */
  CustomResourceDefinitionList getCustomResourceDefinitionList();

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
  <T extends HasMetadata, L extends KubernetesResourceList<?>, D extends Doneable<T>> MixedOperation<T, L, D, Resource<T, D>> customResourcesClient(
      CustomResourceDefinition crd, Class<T> resourceType, Class<L> listClass, Class<D> doneClass);

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
  <T extends HasMetadata, L extends KubernetesResourceList<?>, D extends Doneable<T>> NonNamespaceOperation<T, L, D, Resource<T, D>> customResourcesClientInNameSpace(
      CustomResourceDefinition crd, Class<T> resourceType, Class<L> listClass, Class<D> doneClass,
      String namespace);

  /**
   * Register custom resource definition to kubenetes cluster
   * 
   * @param crd
   */
  void registerCustomResourceDefinition(CustomResourceDefinition crd);

}
