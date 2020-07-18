package org.simulationautomation.kubernetesclient.api;

import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.exceptions.SimulationCreationException;
import io.fabric8.kubernetes.api.model.Pod;

/**
 * This class is the "heart" of te Kubernetes Client for Simulation Automation. It initializes the
 * Kubernetes environment, which includes to set up the watchers for pods and simulation CRDs. Also
 * it restores existing simulations on startup.
 * 
 * @author Niko Benkler
 *
 */
public interface ISimulationOperator {

  /**
   * Init all necessary fields, including simulation namespace, simulation resource definition and
   * watcher for simulation resource definitions and simulation pods
   */
  void init();



  /**
   * Create a simulation custom resource with given name in simulation namespace. </br>
   * 
   * @param zippedExperimentData
   * 
   * @param name
   * @throws SimulationCreationException
   */
  Simulation createNewSimulation(byte[] zippedExperimentData) throws SimulationCreationException;

  /**
   * Find Pod for Simulation with given name. </br>
   * 
   * @param simulationName
   * @return Pod OR null if not present
   */
  Pod getPodBySimulationName(String simulationName);

  /**
   * Delete given pod
   * 
   * @param pod
   */
  void deleteExistingSimulationPod(Pod pod);

}
