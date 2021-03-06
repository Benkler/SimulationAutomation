package org.simulationautomation.kubernetesclient.api;

import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.exceptions.SimulationCreationException;


public interface ISimulationFactory {

  /**
   * Create Simulation and prepare folder structure for the mounting points etc.
   * 
   * @param zippedExperimentData
   * 
   * @return
   * @throws SimulationCreationException
   */
  Simulation createAndPrepareSimulation(byte[] zippedExperimentData)
      throws SimulationCreationException;

}
