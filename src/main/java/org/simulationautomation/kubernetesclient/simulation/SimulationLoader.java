package org.simulationautomation.kubernetesclient.simulation;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.simulationautomation.kubernetesclient.api.ISimulationLoader;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.simulation.properties.SimulationPathFactory;
import org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties;
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

  // TODO adjust -> Only succeeded?
  private static final List<SimulationStatusCode> validStatusForRestoring =
      Arrays.asList(SimulationStatusCode.SUCCEEDED);



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
  public List<Simulation> loadAvailableSimulationsFromMetadata() {
    log.info("Initialize available simulations");
    cleanUpSimulations();
    List<Simulation> availableSimulations = new ArrayList<>();

    File file = new File(SimulationProperties.SIMULATION_BASE_PATH);
    // dir name is equal to simulation name!
    String[] simulationNames = file.list(new FilenameFilter() {
      @Override
      public boolean accept(File current, String name) {
        return new File(current, name).isDirectory();
      }
    });


    Gson gson = new Gson();

    for (String simulationName : simulationNames) {

      log.info("Recover simulation from metadata with name=" + simulationName);
      String pathToSimulationMetaData =
          SimulationPathFactory.getPathToSimulationMetadataFile(simulationName);
      String simulationAsJson = FileUtil.loadFileAsString(pathToSimulationMetaData);
      if (simulationAsJson == null) {
        log.error("Could not recover simulation from metadata with name=" + simulationName);
        continue;
      }
      Simulation recoveredSimulation = gson.fromJson(simulationAsJson, Simulation.class);
      availableSimulations.add(recoveredSimulation);

    }

    return availableSimulations;
  }

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



  /*
   * Clean up corrupt simulation
   */
  private void cleanUpSimulations() {
    log.info("Clean up simulations");
    File file = new File(SimulationProperties.SIMULATION_BASE_PATH);
    Gson gson = new Gson();
    // dir name is equal to simulation name!
    String[] simulationNames = file.list(new FilenameFilter() {
      @Override
      public boolean accept(File current, String name) {
        return new File(current, name).isDirectory();
      }
    });

    for (String simulationName : simulationNames) {
      String pathToSimulationMetaData =
          SimulationPathFactory.getPathToSimulationMetadataFile(simulationName);
      String simulationAsJson = FileUtil.loadFileAsString(pathToSimulationMetaData);
      String pathToSimulation = SimulationPathFactory.getPathToSimulationFolder(simulationName);
      // Delete if no metadata available
      if (simulationAsJson == null) {
        log.info("Simulation with name= " + simulationName
            + " has no metadata and will be deleted from file system");
        FileUtil.deleteDirectory(pathToSimulation);
      }

      Simulation simulation = gson.fromJson(simulationAsJson, Simulation.class);
      SimulationStatusCode status = simulation.getStatus().getStatusCode();


      if (!validStatusForRestoring.contains(status)) {
        log.info("Simulation with name= " + simulationName + " has invalid status= " + status
            + " and will be deleted from file system");
        FileUtil.deleteDirectory(pathToSimulation);
      }

      // TODO any other criteria to delete simulation? What about log file?

    }

  }

}
