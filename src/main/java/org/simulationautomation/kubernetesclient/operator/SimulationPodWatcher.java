package org.simulationautomation.kubernetesclient.operator;

import static org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties.SIMULATION_KIND;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.simulationautomation.kubernetesclient.api.ISimulationOperator;
import org.simulationautomation.kubernetesclient.api.ISimulationPodWatcher;
import org.simulationautomation.kubernetesclient.api.ISimulationServiceRegistry;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.exceptions.SimulationNotFoundException;
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
  static final String POD_PHASE_RUNNING = "Running";
  static final String POD_PHASE_PENDING = "Pending";


  private Logger log = LoggerFactory.getLogger(SimulationPodWatcher.class);

  @Autowired
  private ISimulationServiceRegistry simulationsServiceRegistry;

  @Autowired
  private ISimulationOperator operator;

  private Set<String> resourceVersions =
      Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

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
    } else if (action.equals(Action.DELETED)) {
      handleDeletedAction(pod);
    } else if (action.equals(Action.MODIFIED)) {
      handleModifyAction(pod);
    }

  }

  private void handleModifyAction(Pod pod) {
    String podName = pod.getMetadata().getName();

    log.info("'Modify Pod' Event received for Pod with name= " + podName + " and resource version="
        + pod.getMetadata().getResourceVersion());
    if (pod.getStatus() == null) {
      log.error("Pod status for Pod with name= " + podName + "is null!");
      return;
    }

    Simulation simulation;
    try {
      simulation = getSimulationFromPod(pod);
    } catch (SimulationNotFoundException e) {
      log.error("No simulation found for pod with name=" + podName + ". Cannot update status.");
      return;
    }



    /*
     * Handle modify action and delete pod in case the simulation failed or succeeded.
     */
    switch (pod.getStatus().getPhase()) {
      case POD_PHASE_FAILED:
        simulationsServiceRegistry.updateStatus(simulation, SimulationStatusCode.FAILED);
        operator.deleteExistingSimulationPod(pod);
        break;
      case POD_PHASE_SUCCEEDED:
        simulationsServiceRegistry.updateStatus(simulation, SimulationStatusCode.SUCCEEDED);
        operator.deleteExistingSimulationPod(pod);
        break;
      case POD_PHASE_RUNNING:
        simulationsServiceRegistry.updateStatus(simulation, SimulationStatusCode.RUNNING);
        break;
      case POD_PHASE_PENDING:
        simulationsServiceRegistry.updateStatus(simulation, SimulationStatusCode.PENDING);
      default:
        log.info("Alternative pod phase found: " + pod.getStatus().getPhase());

    }


  }

  private void handleAddAction(Pod pod) {
    log.info("'Add Pod' Event received for Pod with name= " + pod.getMetadata().getName());

  }

  private void handleDeletedAction(Pod pod) {
    log.info("'Delete Pod' Event received for Pod with name= " + pod.getMetadata().getName());
  }

  /*
   * Get accompanying Simulation for given Pod from @SimulationService
   * 
   */
  private Simulation getSimulationFromPod(Pod pod) throws SimulationNotFoundException {
    OwnerReference ownerReference = getControllerOf(pod);

    return simulationsServiceRegistry.getSimulation(ownerReference.getName());

  }

  /*
   * Pod has a Simulation Owner
   */
  private OwnerReference getControllerOf(Pod pod) {
    List<OwnerReference> ownerReferences = pod.getMetadata().getOwnerReferences();
    for (OwnerReference ownerReference : ownerReferences) {
      if (ownerReference.getController()) {
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
