package org.simulationautomation.kubernetesclient.simulation;

import java.util.List;
import org.simulationautomation.kubernetesclient.api.ISimulationOperator;
import org.simulationautomation.kubernetesclient.api.ISimulationServiceProxy;
import org.simulationautomation.kubernetesclient.api.ISimulationServiceRegistry;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.crds.SimulationStatus;
import org.simulationautomation.kubernetesclient.exceptions.SimulationCreationException;
import org.simulationautomation.kubernetesclient.simulation.properties.SimulationPathFactory;
import org.simulationautomation.util.FileUtil;
import org.simulationautomation.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class as Proxy between REST-interface and actual backend for operations on specific
 * simulations.
 * 
 * @author Niko Benkler
 *
 */
@Service
public class SimulationServiceProxy implements ISimulationServiceProxy {


  @Autowired
  private ISimulationServiceRegistry simulationServiceRegistry;

  @Autowired
  private ISimulationOperator operator;


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

    return status == null ? false : status.getStatusCode().equals(SimulationStatusCode.SUCCEEDED);

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
    String pathToZipFile = SimulationPathFactory.getPathToZipFileOfSImulation(simulationName);

    ZipUtil zipUtil = new ZipUtil();
    String zipPath = zipUtil.createZipFileRecursively(pathToOutputFolder, pathToZipFile);

    if (zipPath == null) {
      log.info("Could not create zip file for simulation with name=" + simulationName);
      return null;
    }

    byte[] zipAsByteStream = FileUtil.loadFileAsByteStream(zipPath);
    FileUtil.deleteFile(zipPath);

    return zipAsByteStream;
  }

  @Override
  public byte[] getSimulationResultFile(String simulationName, String fileName) {
    log.info(
        "Get file with name=" + fileName + " of result for simulation with name=" + simulationName);

    String simulationBasePath =
        SimulationPathFactory.getPathToOutputFolderOfSimulation(simulationName);

    return FileUtil.loadFileFromDirectoryRecursively(simulationBasePath, fileName);


  }

  @Override
  public Simulation createSimulation() throws SimulationCreationException {
    return operator.createNewSimulation();
  }

  @Override
  public List<String> getSimulations() {
    return simulationServiceRegistry.getSimulations();
  }


}
