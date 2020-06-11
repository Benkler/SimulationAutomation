package org.simulationautomation.kubernetesclient.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

@Component
public class CustomNamespaceBuilder {

  private static final Logger log = LoggerFactory.getLogger(CustomNamespaceBuilder.class);

  @Autowired
  KubernetesClient client;

  public Namespace createNamespace(String name) {
    Namespace ns = new NamespaceBuilder().withNewMetadata().withName(name).addToLabels("name", name)
        .endMetadata().build();
    Namespace simuNamespace = client.namespaces().createOrReplace(ns);
    log.info("Created namespace=" + simuNamespace.getMetadata().getName());
    return simuNamespace;

  }
}
