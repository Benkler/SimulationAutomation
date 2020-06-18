package org.simulationautomation.kubernetesclient.api;

import java.util.List;
import java.util.Map;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.crds.SimulationStatus;
import org.simulationautomation.kubernetesclient.simulation.SimulationStatusCode;


public interface ISimulationService {

  /**
   * Query all simulations currently stored in this service
   * 
   * @return
   */
  List<String> getSimulations();

  void addSimulation(String simulationName, Simulation simulation);

  Simulation removeSimulation(String simulationName);

  Simulation getSimulation(String simulationName);

  Map<String, Simulation> getSimulationsMap();

  /**
   * Update given simulation status for simulation with given name
   * 
   * @param simulationName
   * @param simulationSatusCode
   */
  void updateStatus(String simulationName, SimulationStatusCode simulationSatusCode);

  SimulationStatus getSimulationStatus(String simulatioUUID);

}
