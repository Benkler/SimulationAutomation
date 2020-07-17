package org.simulationautomation.kubernetesclient.api;

import java.util.List;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.crds.SimulationStatus;
import org.simulationautomation.kubernetesclient.exceptions.SimulationNotFoundException;
import org.simulationautomation.kubernetesclient.simulation.SimulationStatusCode;


public interface ISimulationServiceRegistry {

  /**
   * Query available simulations in cluster.
   * 
   * @return
   */
  List<Simulation> getSimulations();

  /**
   * Query simulation by name
   * 
   * @param simulationName
   * @return
   * @throws SimulationNotFoundException
   */
  Simulation getSimulation(String simulationName) throws SimulationNotFoundException;

  /**
   * Update given simulation status for simulation with given name
   * 
   * @param simulationName
   * @param simulationSatusCode
   */
  void updateStatus(Simulation simulation, SimulationStatusCode simulationSatusCode);

  SimulationStatus getSimulationStatus(String simulationName) throws SimulationNotFoundException;

}
