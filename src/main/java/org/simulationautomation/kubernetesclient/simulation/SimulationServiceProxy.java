package org.simulationautomation.kubernetesclient.simulation;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.simulationautomation.kubernetesclient.api.ISimulationOperator;
import org.simulationautomation.kubernetesclient.api.ISimulationServiceProxy;
import org.simulationautomation.kubernetesclient.api.ISimulationServiceRegistry;
import org.simulationautomation.kubernetesclient.crds.SimulationStatus;
import org.simulationautomation.kubernetesclient.exceptions.RestClientException;
import org.simulationautomation.kubernetesclient.exceptions.SimulationCreationException;
import org.simulationautomation.kubernetesclient.simulation.properties.SimulationPathFactory;
import org.simulationautomation.rest.SimulationVO;
import org.simulationautomation.util.FileUtil;
import org.simulationautomation.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


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
  public SimulationVO createSimulation(MultipartFile file) throws RestClientException {
    log.info("Trying to create simulation from file");

    byte[] zippedExperimentData;
    try {
      zippedExperimentData = file.getBytes();
    } catch (IOException e1) {
      log.error("Error while creating simulation. Error Code=" + e1.getMessage());
      throw new RestClientException("Error while creating Simulation");
    }

    try {
      return SimulationVO.toSimulationVO(operator.createNewSimulation(zippedExperimentData));
    } catch (SimulationCreationException e) {
      log.error("Error while creating simulation. Error Message: " + e.getMessage());

      throw new RestClientException("Error while creating Simulation");
    }
  }

  @Override
  public byte[] getSimulationLog(String simulationName) throws RestClientException {

    checkIfSimulationExists(simulationName);

    log.info("Trying to get log for simulation with name=" + simulationName);
    String pathToLogFile = SimulationPathFactory.getPathToSimulationLogFile(simulationName);

    byte[] logAsByteArray = FileUtil.getInstance().loadFileAsByteStream(pathToLogFile);

    if (logAsByteArray == null) {
      log.info("Could not get log for simulation with name=" + simulationName);
      throw new RestClientException(
          "Log for simulation with name= " + simulationName + " does not exists");
    }

    return logAsByteArray;

  }

  /**
   * Get status of simulation.
   * 
   * @param simulationName
   * @return StatusCode
   * @throws RestClientException
   */
  @Override
  public SimulationStatusCode getSimulationStatus(String simulationName)
      throws RestClientException {
    checkIfSimulationExists(simulationName);


    return simulationServiceRegistry.getSimulationStatus(simulationName).getStatusCode();
  }



  /**
   * Get zipped simulation results as byte array
   * 
   * @param simulationName
   * @return
   * @throws RestClientException
   */
  @Override
  public byte[] getSimulationResults(String simulationName) throws RestClientException {

    checkIfSimulationExists(simulationName);
    checkIfSimulationIsFinished(simulationName);

    log.info("Get simulation results for simulation with name=" + simulationName);
    String pathToOutputFolder =
        SimulationPathFactory.getPathToOutputFolderOfSimulation(simulationName);
    // Path and name for zip file -> SimulationResults.zip
    String pathToZipFile = SimulationPathFactory.getPathToZipFileOfSImulation(simulationName);


    String zipPath =
        ZipUtil.getInstance().zipDirectoryRecursively(pathToOutputFolder, pathToZipFile);

    if (zipPath == null) {
      log.info("Could not create zip file for simulation with name=" + simulationName);
      return null;
    }

    byte[] zipAsByteStream = FileUtil.getInstance().loadFileAsByteStream(zipPath);
    FileUtil.getInstance().deleteFile(zipPath);



    return zipAsByteStream;
  }

  @Override
  public byte[] getSimulationResultFile(String simulationName, String fileName)
      throws RestClientException {
    checkIfSimulationExists(simulationName);
    checkIfSimulationIsFinished(simulationName);
    log.info(
        "Get file with name=" + fileName + " of result for simulation with name=" + simulationName);

    String simulationBasePath =
        SimulationPathFactory.getPathToOutputFolderOfSimulation(simulationName);

    byte[] resultFile =
        FileUtil.getInstance().loadFileFromDirectoryRecursively(simulationBasePath, fileName);

    if (resultFile == null) {
      throw new RestClientException(
          "Simulation with name=" + simulationName + " does not have a file with name=" + fileName);
    }

    return resultFile;


  }

  @Override
  public List<SimulationVO> getSimulations() {
    return simulationServiceRegistry.getSimulations().stream().map(SimulationVO::toSimulationVO)
        .collect(Collectors.toList());
  }

  private boolean isSimulationFinished(String simulationName) {

    SimulationStatus status = simulationServiceRegistry.getSimulationStatus(simulationName);

    return status == null ? false : status.getStatusCode().equals(SimulationStatusCode.SUCCEEDED);

  }

  private void checkIfSimulationIsFinished(String simulationName) throws RestClientException {
    if (!isSimulationFinished(simulationName)) {
      throw new RestClientException(
          "Simulation with name= " + simulationName + " is not yet finished");
    }


  }



  private boolean doesSimulationExist(String simulationName) {
    return simulationServiceRegistry.getSimulation(simulationName) != null;
  }

  private void checkIfSimulationExists(String simulationName) throws RestClientException {
    if (!doesSimulationExist(simulationName)) {
      throw new RestClientException("Simulation with name= " + simulationName + " does not exist");
    }
  }


}
