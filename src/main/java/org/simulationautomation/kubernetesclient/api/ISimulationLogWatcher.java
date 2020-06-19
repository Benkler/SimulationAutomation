package org.simulationautomation.kubernetesclient.api;

import org.simulationautomation.kubernetesclient.crds.Simulation;
import io.fabric8.kubernetes.api.model.Pod;


public interface ISimulationLogWatcher {

  /**
   * Register LogWatcher for a given pod of a simulation. Logs are written into a file, which is
   * saved within the accompanying simulation folder. Thread is necessary, as we need to wait for
   * the pod to be ready/complete, in order to start watch/close the file output stream.
   * 
   * @param simulation
   * @param simulationPod
   */
  void registerSimulationPodLogWatcher(Simulation simulation, Pod simulationPod);

}
