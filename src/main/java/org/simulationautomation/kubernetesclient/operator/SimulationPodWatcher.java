package org.simulationautomation.kubernetesclient.operator;

import static org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties.SIMULATION_KIND;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.simulationautomation.kubernetesclient.api.ISimulationPodWatcher;
import org.simulationautomation.kubernetesclient.api.ISimulationServiceRegistry;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.simulation.SimulationStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;

@Component
public class SimulationPodWatcher implements ISimulationPodWatcher {

  static final String POD_PHASE_SUCCEEDED = "Succeeded";
  static final String POD_PHASE_FAILED = "Failed";

  private Logger log = LoggerFactory.getLogger(SimulationPodWatcher.class);

  @Autowired
  private ISimulationServiceRegistry simulationsService;

  private Set<String> resourceVersions = new HashSet<>();

  @Override
  public void eventReceived(Watcher.Action action, Pod pod) {

    if (alreadyReceivedEvent(pod)) {
      return;
    }

    /*
     * This Watcher should only care about Pods that have a valid owner and are from Type Simulation
     */
    OwnerReference ownerReference = getControllerOf(pod);
    if (ownerReference == null || !ownerReference.getKind().equalsIgnoreCase(SIMULATION_KIND)) {
      return;
    }

    if (action.equals(Action.ADDED)) {
      handleAddAction(pod);
    } else if (action.equals(Action.MODIFIED)) {
      handleModifyAction(pod);
    } else if (action.equals(Action.DELETED)) {
      handleDeletedAction(pod);
    }

  }

  private void handleModifyAction(Pod pod) {
    String podName = pod.getMetadata().getName();

    log.info("'Modify Pod' Event received for Pod with name= " + podName);
    if (pod.getStatus() == null) {
      log.error("Pod status for Pod with name= " + podName + "is null!");
      return;
    }

    Simulation simulation = getSimulationFromPod(pod);
    if (simulation == null) {
      log.error("No simulation found for pod with name=" + podName + ". Cannot update status.");
    }


    log.info("Pod with name " + podName + " is currently in phase " + pod.getStatus().getPhase());

    switch (pod.getStatus().getPhase()) {
      case POD_PHASE_FAILED:
        simulationsService.updateStatus(simulation.getMetadata().getName(),
            SimulationStatusCode.FAILED);
        break;
      case POD_PHASE_SUCCEEDED:
        simulationsService.updateStatus(simulation.getMetadata().getName(),
            SimulationStatusCode.SUCCEEDED);
        break;
      default:

    }



  }

  private void handleDeletedAction(Pod pod) {
    log.info("'Delete Pod' Event received for Pod with name= " + pod.getMetadata().getName());
  }

  private void handleAddAction(Pod pod) {
    log.info("'Add Pod' Event received for Pod with name= " + pod.getMetadata().getName());
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
    if (simulation == null) {
      log.info("Simulation not found for pod with name: " + pod.getMetadata().getName());
    }

    return simulation;
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
   * Filter event, which were already received. Necessary due to k8s cache events behaviour
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

  /**
   * Close kubernetes operator pod
   */
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



}
