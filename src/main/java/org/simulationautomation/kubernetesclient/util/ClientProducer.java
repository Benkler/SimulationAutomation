package org.simulationautomation.kubernetesclient.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

@Configuration
public class ClientProducer {

  private static final Logger log = LoggerFactory.getLogger(ClientProducer.class);

  @Bean(name = "namespace")
  @Scope("singleton")
  public String findMyCurrentNameSpace() {

    try {
      String nameSpace = new String(
          Files.readAllBytes(Paths.get("var/run/secrets/kubernetes.io/serviceaccount/namespace")));
      log.info("Current namespace=" + nameSpace);
      return nameSpace;
    } catch (IOException e) {
      log.warn(
          "Error while retrieving namespace. Message: " + e + "\n Fallback to namespace 'default'");
      return "default";
    }

  }

  @Bean
  @Scope("singleton")
  public KubernetesClient makeDefaultClient(@Qualifier("namespace") String namespace) {

    log.info("Kubernetes Client created in namespace=" + namespace);

    return new DefaultKubernetesClient().inNamespace(namespace);
    // return new DefaultKubernetesClient();

  }

}
