package org.simulationautomation.kubernetesclient.simulation;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.simulationautomation.kubernetesclient.api.ISimulationFactory;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.crds.SimulationSpec;
import org.simulationautomation.kubernetesclient.crds.SimulationStatus;
import org.simulationautomation.kubernetesclient.exceptions.SimulationCreationException;
import org.simulationautomation.kubernetesclient.simulation.properties.SimulationPathFactory;
import org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties;
import org.simulationautomation.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import io.fabric8.kubernetes.api.model.ObjectMeta;

@Service
public class SimulationFactory implements ISimulationFactory {



  private Logger log = LoggerFactory.getLogger(SimulationFactory.class);


  @Override
  public Simulation createAndPrepareSimulation(byte[] zippedExperimentData)
      throws SimulationCreationException {

    // Prepare folder structure and provide input which will be mounted into simulation pod
    String simulationName = generateCustomUUID();
    prepareFolderStructure(simulationName);
    prepareInput(simulationName, zippedExperimentData);


    Simulation simuCR = new Simulation();

    // Generate Metadata
    ObjectMeta metaData = new ObjectMeta();
    metaData.setName(simulationName);
    metaData.setNamespace(SimulationProperties.SIMULATION_NAMESPACE);

    // Generate Spec
    SimulationSpec simuSpec = new SimulationSpec();

    simuSpec.setSimulationFileName(getExperimentFileNameFromZipFile(zippedExperimentData));

    // Add to Simulation
    simuCR.setMetadata(metaData);
    simuCR.setSpec(simuSpec);


    SimulationStatus status = new SimulationStatus();
    status.setStatus(SimulationStatusCode.CREATED);
    simuCR.setStatus(status);

    // TODO set further spec

    return simuCR;
  }


  private String getExperimentFileNameFromZipFile(byte[] zippedExperimentData)
      throws SimulationCreationException {
    log.info("Trying to get experiment file name from zip");
    List<String> experimentFileNames;
    try {
      experimentFileNames = ZipUtil.getInstance().getFileNamesFromZipFileWithExtension(
          zippedExperimentData, SimulationProperties.SIMULATION_EXPERIMENT_EXTENSION);
    } catch (IOException e) {
      log.error(
          "Error while getting experiment file name from zip. Error message= " + e.getMessage());
      throw new SimulationCreationException("");
    }

    if (experimentFileNames.isEmpty()) {
      throw new SimulationCreationException(
          "Cannot create simulation, as no experiments files is included in simulation data");
    }

    String experimentFileName = experimentFileNames.get(0);
    if (experimentFileNames.size() > 1) {
      log.info(
          "ATTENTION: More than one experiment file is included. Following experiment is executed: "
              + experimentFileName);
    }
    log.info("Successfully experiment file with name= " + experimentFileName);


    return experimentFileName;
  }


  /**
   * 
   * 
   * @param simulationName
   * @param zippedExperimentData
   * @throws SimulationCreationException
   * @throws IOException
   */
  private void prepareInput(String simulationName, byte[] zippedExperimentData)
      throws SimulationCreationException {

    log.info("Trying to prepare experiment data for simulation with name=" + simulationName);
    try {
      ZipUtil.getInstance().extractZipToDestinationDir(zippedExperimentData,
          SimulationPathFactory.getPathToInputFolderOfSimulation(simulationName));
    } catch (IOException e) {
      log.error("Error while exracting zipped experiment data. Error message=\n" + e.getMessage());
      throw new SimulationCreationException(
          "Could not create Simulation. Error while extracting zipped experiment data");
    }


    log.info("Sucessfully extracted experiment data for simulation with name=" + simulationName);

  }


  /**
   * - Prepare folder structure for mounting points: </br>
   * - InputFolder will be mounted by @SimulationPodCreator to location in docker simulation image,
   * where it expects the simulation input </br>
   * - OutputFolder will be mounted by @SimulationPodCreator to location in docker simulation image,
   * where it will put the result of the Simulation </br>
   * - As soon as the Simulation is finished, the application can collect the results.
   * 
   */
  private void prepareFolderStructure(String simulationName) {

    String pathToInputFolder =
        SimulationPathFactory.getPathToInputFolderOfSimulation(simulationName);

    String pathToOutputFolder =
        SimulationPathFactory.getPathToOutputFolderOfSimulation(simulationName);

    new File(pathToInputFolder).mkdirs();
    new File(pathToOutputFolder).mkdirs();

    log.info("Successfully create Folder Strucutre for simulation with name=" + simulationName);

  }



  /**
   * UUID without dashes, as k8s adds dash to name when generating a pod </br>
   * Example: Generating uuid: simulation-abcdefg1234567 </br>
   * Kubernetes appends: simulation-abcdefg1234567-hijklmnop89
   * 
   */
  private String generateCustomUUID() {
    return "simulation-" + UUID.randomUUID().toString().replace("-", "");

  }


}
