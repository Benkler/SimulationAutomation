package org.simulationautomation.kubernetesclient.api;

import org.simulationautomation.kubernetesclient.crds.Simulation;
import io.fabric8.kubernetes.api.model.Pod;

/**
 * Watch logs of a simulation.
 * 
 * @author Niko Benkler
 *
 */
public interface ISimulationLogWatcher {

  /**
   * Watch the logs of a pod that is executing a simulation.
   * 
   * @param simulation
   * @param simulationPod
   */
  void registerSimulationPodLogWatcher(Simulation simulation, Pod simulationPod);

}
