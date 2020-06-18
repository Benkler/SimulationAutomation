package org.simulationautomation.kubernetesclient.operator;

import org.simulationautomation.kubernetesclient.api.ISimulationWatcher;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;

@Component
public class SimulationWatcher implements ISimulationWatcher {

  private Logger log = LoggerFactory.getLogger(SimulationWatcher.class);


  @Override
  public void eventReceived(Watcher.Action action, Simulation simulation) {
    String simulationName = simulation.getMetadata().getName();



    if (action.equals(Action.ADDED)) {
      log.info("'Add Simulation' Event received for simulation with name= " + simulationName);

    }

    if (action.equals(Action.MODIFIED)) {
      log.info("'Modifiy Simulation' Event received for simulation with name= " + simulationName);

    }

    if (action.equals(Action.DELETED)) {
      log.info("'Delete Simulation' Event received for simulation with name= " + simulationName);

    }

    if (simulation.getSpec() == null) {
      log.info("No Spec for resource " + simulation.getMetadata().getName());
    }
  }

  @Override
  public void onClose(KubernetesClientException cause) {

    // cause != null was due to issue like network connection loss
    if (cause != null) {
      cause.printStackTrace();
      // End pod
      System.exit(-1);
    }
  }

}
