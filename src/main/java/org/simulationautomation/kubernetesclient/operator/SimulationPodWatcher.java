package org.simulationautomation.kubernetesclient.operator;

import static org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties.SIMULATION_KIND;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.simulation.SimulationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;

@Component
public class SimulationPodWatcher implements Watcher<Pod> {

  private Logger log = LoggerFactory.getLogger(SimulationPodWatcher.class);

  @Autowired
  private SimulationService simulationsService;

  private Set<String> resourceVersions = new HashSet<>();

  @Override
  public void eventReceived(Watcher.Action action, Pod pod) {

    String podName = pod.getMetadata().getName();

    if (alreadyReceivedEvent(pod)) {
      return;
    }

    OwnerReference ownerReference = getControllerOf(pod);

    if (ownerReference == null || !ownerReference.getKind().equalsIgnoreCase(SIMULATION_KIND)) {
      /*
       * This Watcher should only care about Pods that have a valid owner and are from Type
       * Simulation
       */
      return;
    }

    // TODO do sth with simulation
    Simulation simulation = getSimulationFromPod(pod);


    if (action.equals(Action.ADDED)) {
      log.info("'Add Pod' Event received for Pod with name= " + podName);

    }

    if (action.equals(Action.MODIFIED)) {

      log.info("'Modify Pod' Event received for Pod with name= " + podName);

    }

    if (action.equals(Action.DELETED)) {
      log.info("'Delete Pod' Event received for Pod with name= " + podName);

    }

    if (pod.getSpec() == null) {
      log.info("No Spec for Pod with name= " + podName);
    }
  }

  /**
   * Get accompanying Simulation for given Pod from @SimulationService
   * 
   * @param pod
   * @return
   */
  private Simulation getSimulationFromPod(Pod pod) {
    OwnerReference ownerReference = getControllerOf(pod);

    Simulation simulation = simulationsService.getSimulation(ownerReference.getName());
    if (simulation != null) {
      log.info("Simulation found for pod with name: " + pod.getMetadata().getName());

    } else {
      log.info("Simulation not found for pod with name: " + pod.getMetadata().getName());
    }

    return simulation;
  }

  @Override
  public void onClose(KubernetesClientException cause) {

    log.info("Pod closed with cause = " + cause.toString());
    // cause != null was due to issue like network connection loss
    if (cause != null) {
      cause.printStackTrace();
      // End pod
      System.exit(-1);
    }
  }

  /*
   * Pod has a Simulation Owner
   */
  private OwnerReference getControllerOf(Pod pod) {
    List<OwnerReference> ownerReferences = pod.getMetadata().getOwnerReferences();
    for (OwnerReference ownerReference : ownerReferences) {
      if (ownerReference.getController().equals(Boolean.TRUE)) {
        return ownerReference;
      }
    }
    return null;
  }

  /*
   * Filter event, which were already received. Necessary due to k8s behaviour
   */
  private boolean alreadyReceivedEvent(Pod pod) {

    // Pod plus ResourceVersion identifies already received event
    String podAndResourceVersion =
        pod.getMetadata().getUid() + pod.getMetadata().getResourceVersion();
    if (resourceVersions.contains(podAndResourceVersion)) {
      return true;
    } else {
      resourceVersions.add(podAndResourceVersion);
      return false;
    }

  }

}
