package org.simulationautomation.kubernetesclient.api;

import java.util.List;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.crds.SimulationStatus;
import org.simulationautomation.kubernetesclient.simulation.SimulationStatusCode;


public interface ISimulationServiceRegistry {

  /**
   * Query all simulations currently stored in this service
   * 
   * @return
   */
  List<Simulation> getSimulations();

  Simulation getSimulation(String simulationName);

  /**
   * Update given simulation status for simulation with given name
   * 
   * @param simulationName
   * @param simulationSatusCode
   */
  void updateStatus(Simulation simulation, SimulationStatusCode simulationSatusCode);

  SimulationStatus getSimulationStatus(String simulatioUUID);

}
