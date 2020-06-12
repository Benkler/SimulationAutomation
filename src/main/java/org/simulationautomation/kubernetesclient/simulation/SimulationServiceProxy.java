package org.simulationautomation.kubernetesclient.simulation;

import org.simulationautomation.kubernetesclient.crds.SimulationStatus;
import org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties;
import org.simulationautomation.util.ZipFolderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SimulationServiceProxy {

  @Autowired
  SimulationService simulationService;


  public boolean isSimulationFinished(String simulationName) {

    SimulationStatus status = simulationService.getSimulationStatus(simulationName);

    return status == null ? false : status.getStatus().equals(SimulationStatusCode.SUCCEEDED);

  }


  public boolean doesSimulationExist(String simulationName) {
    return simulationService.getSimulation(simulationName) != null;
  }

  public void getSimulationResults(String simulationName) {
    String pathToOutputFolder = SimulationProperties.SIMULATION_BASE_PATH + "/" + simulationName
        + "/" + SimulationProperties.SIMULATION_OUTPUT_FOLDER_NAME;

    String destinationPath = SimulationProperties.SIMULATION_BASE_PATH + "/" + simulationName;
    ZipFolderUtil zipUtil = new ZipFolderUtil();
    // TODO
    String zipPath = zipUtil.zipFolderRecursively(pathToOutputFolder, destinationPath);
  }



}
