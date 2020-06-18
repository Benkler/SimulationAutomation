package org.simulationautomation.kubernetesclient.api;

import java.util.List;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.exceptions.SimulationCreationException;


public interface ISimulationOperator {

  /**
   * Init all necessary fields, including simulation namespace, simulation resource definition and
   * watcher for simulation resource definitions and simulation pods
   */
  void init();

  /**
   * List custom Resources for type Simulation in given namepsace
   * 
   * @param namespace
   * @return
   * @return
   */
  List<Simulation> listExistingSimulations();

  /**
   * Create a simulation custom resource with given name in simulation namespace. </br>
   * 
   * @param name
   * @throws SimulationCreationException
   */
  // TODO further parameters necessary
  Simulation createSimulation() throws SimulationCreationException;

}