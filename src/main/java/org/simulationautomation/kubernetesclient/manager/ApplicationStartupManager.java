package org.simulationautomation.kubernetesclient.manager;

import org.simulationautomation.kubernetesclient.api.ISimulationOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import io.fabric8.kubernetes.client.KubernetesClient;

@Component
public class ApplicationStartupManager implements ApplicationListener<ApplicationReadyEvent> {

  @Autowired
  ISimulationOperator simulationOperator;

  @Autowired
  KubernetesClient client;

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {

    simulationOperator.init();

  }



}
