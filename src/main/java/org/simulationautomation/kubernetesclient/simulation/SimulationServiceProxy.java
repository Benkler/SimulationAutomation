package org.simulationautomation.kubernetesclient.simulation;

import org.simulationautomation.kubernetesclient.api.ISimulationServiceProxy;
import org.simulationautomation.kubernetesclient.api.ISimulationServiceRegistry;
import org.simulationautomation.kubernetesclient.crds.SimulationStatus;
import org.simulationautomation.kubernetesclient.simulation.properties.SimulationPathFactory;
import org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties;
import org.simulationautomation.util.FileUtil;
import org.simulationautomation.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class as Proxy between REST-interface and actual backend
 * 
 * @author Niko Benkler
 *
 */
@Service
public class SimulationServiceProxy implements ISimulationServiceProxy {


  @Autowired
  ISimulationServiceRegistry simulationServiceRegistry;



  private static final Logger log = LoggerFactory.getLogger(SimulationServiceProxy.class);


  @Override
  public byte[] getSimulationLog(String simulationName) {

    log.info("Trying to get log for simulation with name=" + simulationName);
    String pathToLogFile = SimulationPathFactory.getPathToSimulationLogFile(simulationName);

    byte[] log = FileUtil.loadFileAsByteStream(pathToLogFile);



    return log;



  }

  @Override
  public boolean isSimulationFinished(String simulationName) {

    SimulationStatus status = simulationServiceRegistry.getSimulationStatus(simulationName);

    return status == null ? false : status.getStatus().equals(SimulationStatusCode.SUCCEEDED);

  }


  @Override
  public boolean doesSimulationExist(String simulationName) {
    return simulationServiceRegistry.getSimulation(simulationName) != null;
  }

  /**
   * Get zipped simulation results as byte array
   * 
   * @param simulationName
   * @return
   */
  @Override
  public byte[] getSimulationResults(String simulationName) {

    log.info("Get simulation results for simulation with name=" + simulationName);
    String pathToOutputFolder =
        SimulationPathFactory.getPathToOutputFolderOfSimulation(simulationName);
    // Path and name for zip file -> SimulationResults.zip
    String destinationPath =
        SimulationProperties.SIMULATION_BASE_PATH + "/" + simulationName + "/SimulationResults";

    ZipUtil zipUtil = new ZipUtil();
    String zipPath = zipUtil.createZipFileRecursively(pathToOutputFolder, destinationPath);

    if (zipPath == null) {
      log.info("Could not create zip file for simulation with name=" + simulationName);
      return null;
    }

    byte[] zipAsByteStream = FileUtil.loadFileAsByteStream(zipPath);
    FileUtil.deleteFile(zipPath);

    return zipAsByteStream;
  }



}
