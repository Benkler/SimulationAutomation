package org.simulationautomation.kubernetesclient.simulation;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.simulationautomation.kubernetesclient.api.ISimulationFactory;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.crds.SimulationSpec;
import org.simulationautomation.kubernetesclient.crds.SimulationStatus;
import org.simulationautomation.kubernetesclient.exceptions.SimulationCreationException;
import org.simulationautomation.kubernetesclient.simulation.properties.SimulationPathFactory;
import org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import io.fabric8.kubernetes.api.model.ObjectMeta;

@Service
public class SimulationFactory implements ISimulationFactory {


  private Logger log = LoggerFactory.getLogger(SimulationFactory.class);


  @Override
  public Simulation createAndPrepareSimulation() throws SimulationCreationException {

    // Prepare folder structure and provide input which will be mounted into simulation pod
    String simulationName = generateCustomUUID();
    prepareFolderStructure(simulationName);
    prepareInput(simulationName);


    Simulation simuCR = new Simulation();

    // Generate Metadata
    ObjectMeta metaData = new ObjectMeta();
    metaData.setName(simulationName);
    metaData.setNamespace(SimulationProperties.SIMULATION_NAMESPACE);

    // Generate Spec
    SimulationSpec simuSpec = new SimulationSpec();

    // Add to Simulation
    simuCR.setMetadata(metaData);
    simuCR.setSpec(simuSpec);


    SimulationStatus status = new SimulationStatus();
    status.setStatus(SimulationStatusCode.CREATED);
    simuCR.setStatus(status);

    // TODO set further spec

    return simuCR;
  }


  /**
   * TODO needs dynamic Input
   * 
   * @param simulationName
   * @throws SimulationCreationException
   * @throws IOException
   */
  private void prepareInput(String simulationName) throws SimulationCreationException {



    File srcDir = new File(SimulationProperties.SIMULATION_EXPERIMENT_FILES_PATH);
    File destDir = new File(SimulationPathFactory.getPathToInputFolderOfSimulation(simulationName));

    try {
      FileUtils.copyDirectory(srcDir, destDir);
    } catch (IOException e) {
      log.error("Could not copy input to destination for simulation with name=" + simulationName);
      throw new SimulationCreationException(e.getMessage(), e);
    }

    log.info("Successfully copied experiment data to destination for simulation with name="
        + simulationName);

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
