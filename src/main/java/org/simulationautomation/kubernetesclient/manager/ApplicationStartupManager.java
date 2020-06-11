package org.simulationautomation.kubernetesclient.manager;

import static org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties.SIMULATION_NAMESPACE;

import org.simulationautomation.kubernetesclient.operator.SimulationOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.client.KubernetesClient;

@Component
public class ApplicationStartupManager implements ApplicationListener<ApplicationReadyEvent> {

  static final String IMAGE_PULL_POLICY = "IfNotPresent";
  private static final Logger log = LoggerFactory.getLogger(ApplicationStartupManager.class);

  @Autowired
  SimulationOperator simulationOperator;

  @Autowired
  KubernetesClient client;

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {

    clearCluster();
    simulationOperator.init();

  }

  private void clearCluster() {
    // Remove old pods
    client.pods().inNamespace(SIMULATION_NAMESPACE).delete();

  }

}
