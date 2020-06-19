package org.simulationautomation.kubernetesclient.api;

import java.util.List;
import org.simulationautomation.kubernetesclient.crds.Simulation;


public interface ISimulationLoader {

  /**
   * Create Simulation-Object from metadata file (persisted on file system). </br>
   * 
   * 
   * @param simulationName
   * @return simulation OR null if not found
   */
  Simulation loadSimulationFromMetadata(String simulationName);

  /**
   * Update simulation metadata: Simulation will be serialized into json format and stored in file
   * at predefined path for the accompanying simulation.
   * 
   * @param simulation
   */
  void updateSimulationMetadata(Simulation simulation);

  /**
   * Get all simulation from metadata, which are found on the file system.
   * 
   * @return
   */
  List<Simulation> getAvailableSimulationsFromMetadata();

  /**
   * Clean up simulation folder: Delete simulations which were interrupted during execution.
   */
  void cleanUpSimulations();

}
