package org.simulationautomation.kubernetesclient.api;

import java.util.List;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.exceptions.SimulationCreationException;

public interface ISimulationServiceProxy {

  boolean isSimulationFinished(String simulationName);

  boolean doesSimulationExist(String simulationName);

  /**
   * Get zipped simulation results as byte array
   * 
   * @param simulationName
   * @return
   */
  byte[] getSimulationResults(String simulationName);

  byte[] getSimulationLog(String simulationName);

  Simulation createSimulation(byte[] zippedExperimentData) throws SimulationCreationException;

  List<Simulation> getSimulations();

  byte[] getSimulationResultFile(String simulationName, String fileName);


}
