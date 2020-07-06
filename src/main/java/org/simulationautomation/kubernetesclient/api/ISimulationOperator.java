package org.simulationautomation.kubernetesclient.api;

import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.exceptions.SimulationCreationException;
import io.fabric8.kubernetes.api.model.Pod;


public interface ISimulationOperator {

  /**
   * Init all necessary fields, including simulation namespace, simulation resource definition and
   * watcher for simulation resource definitions and simulation pods
   */
  void init();



  /**
   * Create a simulation custom resource with given name in simulation namespace. </br>
   * 
   * @param name
   * @throws SimulationCreationException
   */
  // TODO further parameters necessary
  Simulation createNewSimulation() throws SimulationCreationException;

  /**
   * Find Pod for Simulation with given name. </br>
   * 
   * @param simulationName
   * @return Pod OR null if not present
   */
  Pod getPodBySimulationName(String simulationName);

  void deleteExistingSimulationPod(Pod pod);

}
