package org.simulationautomation.controller;

import java.util.List;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.exceptions.SimulationCreationException;
import org.simulationautomation.kubernetesclient.operator.SimulationOperator;
import org.simulationautomation.kubernetesclient.util.CustomNamespaceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.P;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

@RestController
@Component
public class WelcomeController {

  private static final Logger log = LoggerFactory.getLogger(WelcomeController.class);

  @Autowired
  KubernetesClient client;

  @Autowired
  CustomNamespaceBuilder nsBuilder;

  @Autowired
  SimulationOperator operator;

  @RequestMapping("/greeting")
  public String greeting() {
    log.info("RestEndpoint triggered");

    Div div = new Div();

    List<Pod> podList = client.pods().list().getItems();

    P p = new P().appendText("Welcome! There are " + podList.size()
        + " pods registered in namespace:" + client.getNamespace());
    div.appendChild(p);

    for (Pod pod : podList) {
      StringBuilder sb = new StringBuilder();
      String podName = pod.getMetadata().getName();
      String podUid = pod.getMetadata().getUid();
      sb.append("pod name=" + podName + "\n");
      sb.append("		pod id=" + podUid + "\n");

      P podP = new P();
      podP.appendText(sb.toString());
      div.appendChild(podP);
    }

    return div.write();
  }

  // @PostMapping("/create/{simName}")
  @RequestMapping("/create")
  public String createRessource() {

    Simulation simulation;
    try {
      simulation = operator.createSimulation();
    } catch (SimulationCreationException e) {
      return "Could not create simulation. Error Message=" + e.getMessage();
    }


    return "Created simulation with name simName= " + simulation.getMetadata().getName();
  }

  @RequestMapping("/list")
  public String list() {

    return "";
  }

}
