package org.simulationautomation.kubernetesclient.api;

import io.fabric8.kubernetes.api.model.Namespace;


public interface ICustomNameSpaceBuilder {

  Namespace createNamespace(String name);

}
