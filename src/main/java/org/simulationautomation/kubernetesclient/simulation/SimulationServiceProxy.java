package org.simulationautomation.kubernetesclient.simulation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.simulationautomation.kubernetesclient.crds.SimulationStatus;
import org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties;
import org.simulationautomation.rest.SimulationRestController;
import org.simulationautomation.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SimulationServiceProxy {

  @Autowired
  SimulationService simulationService;

  private static final Logger log = LoggerFactory.getLogger(SimulationRestController.class);


  public boolean isSimulationFinished(String simulationName) {

    SimulationStatus status = simulationService.getSimulationStatus(simulationName);

    return status == null ? false : status.getStatus().equals(SimulationStatusCode.SUCCEEDED);

  }


  public boolean doesSimulationExist(String simulationName) {
    return simulationService.getSimulation(simulationName) != null;
  }

  /**
   * Get zipped simulation results as byte array
   * 
   * @param simulationName
   * @return
   */
  public byte[] getSimulationResults(String simulationName) {

    log.info("Get simulation results for simulation with name=" + simulationName);
    String pathToOutputFolder = SimulationProperties.SIMULATION_BASE_PATH + "/" + simulationName
        + "/" + SimulationProperties.SIMULATION_OUTPUT_FOLDER_NAME;

    // Path and name for zip file -> SimulationResults.zip
    String destinationPath =
        SimulationProperties.SIMULATION_BASE_PATH + "/" + simulationName + "/SimulationResults";

    ZipUtil zipUtil = new ZipUtil();
    String zipPath = zipUtil.createZipFileRecursively(pathToOutputFolder, destinationPath);

    if (zipPath == null) {
      log.info("Could not create zip file for simulation with name=" + simulationName);
      return null;
    }

    return loadZipFile(zipPath);
  }


  private byte[] loadZipFile(String zipPath) {

    log.info("Load zip file at path=" + zipPath);

    // Load Zip File
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    File zipFile = new File(zipPath);
    FileInputStream fis;

    try {
      fis = new FileInputStream(zipFile);
      org.apache.commons.io.IOUtils.copy(fis, byteArrayOutputStream);
    } catch (IOException e) {
      log.error("Error while reading zip file.", e);
      return null;
    }



    return byteArrayOutputStream.toByteArray();

  }



}
