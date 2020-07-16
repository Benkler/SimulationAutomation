package org.simulationautomation.kubernetesclient.api;

import java.util.List;
import org.simulationautomation.kubernetesclient.exceptions.RestClientException;
import org.simulationautomation.kubernetesclient.simulation.SimulationStatusCode;
import org.simulationautomation.rest.SimulationVO;
import org.springframework.web.multipart.MultipartFile;

public interface ISimulationServiceProxy {



  /**
   * Get zipped simulation results as byte array
   * 
   * @param simulationName
   * @return
   * @throws RestClientException
   */
  byte[] getSimulationResults(String simulationName) throws RestClientException;

  byte[] getSimulationLog(String simulationName) throws RestClientException;

  SimulationVO createSimulation(MultipartFile file) throws RestClientException;

  List<SimulationVO> getSimulations();

  byte[] getSimulationResultFile(String simulationName, String fileName) throws RestClientException;

  SimulationStatusCode getSimulationStatus(String simulationName) throws RestClientException;


}
