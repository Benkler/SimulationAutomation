package org.simulationautomation.kubernetesclient.simulation;

import java.util.List;
import org.simulationautomation.kubernetesclient.api.ISimulationLoader;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.simulation.properties.SimulationPathFactory;
import org.simulationautomation.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;

/**
 * This class loads and saves Simulation objects from/to persistent file system. </br>
 * It is used to restore the state of the SimulationAutomation Client after a crash.
 * 
 * @author Niko Benkler
 *
 */
@Component
public class SimulationLoader implements ISimulationLoader {

  private static final Logger log = LoggerFactory.getLogger(SimulationLoader.class);

  /**
   * Create Simulation-Object from metadata file (persisted on file system). </br>
   * 
   * 
   * @param simulationName
   * @return simulation OR null if not found
   */
  @Override
  public Simulation loadSimulationFromMetadata(String simulationName) {

    log.info("Trying to load metadata of simulation with name=" + simulationName);
    String json = FileUtil
        .loadFileAsString(SimulationPathFactory.getPathToSimulationMetadataFile(simulationName));

    if (json == null) {
      log.info("Could not load metadata for simulation with name=" + simulationName);
      return null;
    }

    Gson gson = new Gson();
    Simulation simulation = gson.fromJson(json, Simulation.class);
    return simulation;
  }


  /**
   * Update simulation metadata: Simulation will be serialized into json format and stored in file
   * at predefined path for the accompanying simulation.
   * 
   * @param simulation
   */
  @Override
  public void updateSimulationMetadata(Simulation simulation) {
    String simulationName = simulation.getMetadata().getName();
    log.info("Trying to update metadata of simulation with name=" + simulationName);
    Gson gson = new Gson();
    String json = gson.toJson(simulation);
    String pathToMetadataFile =
        SimulationPathFactory.getPathToSimulationMetadataFile(simulationName);

    if (FileUtil.createFileFromString(pathToMetadataFile, json)) {
      log.info("Successfully updated metadata of simulation with name=" + simulationName);
    } else {
      log.info("Could not to update metadata of simulation with name=" + simulationName);
    }


  }

  /**
   * Get all simulation from metadata, which are found on the file system.
   * 
   * @return
   */
  @Override
  public List<Simulation> getAvailableSimulationsFromMetadata() {


    return null;
  }

  /**
   * Clean up simulation folder: Delete simulations which were interrupted during execution.
   */
  @Override
  public void cleanUpSimulations() {

  }

}
