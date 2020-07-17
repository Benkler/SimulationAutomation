package org.simulationautomation.kubernetesclient.api;

import java.util.List;
import org.simulationautomation.kubernetesclient.exceptions.RestClientException;
import org.simulationautomation.kubernetesclient.rest.SimulationVO;
import org.simulationautomation.kubernetesclient.simulation.SimulationStatusCode;
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

  /**
   * Get simulation Log for simulation with given name
   * 
   * @param simulationName
   * @return
   * @throws RestClientException
   */
  byte[] getSimulationLog(String simulationName) throws RestClientException;

  /**
   * Create simulation from transmitted experiment data
   * 
   * @param file
   * @return
   * @throws RestClientException
   */
  SimulationVO createSimulation(MultipartFile file) throws RestClientException;

  /**
   * Get all available simulations
   * 
   * @return
   */
  List<SimulationVO> getSimulations();

  byte[] getSimulationResultFile(String simulationName, String fileName) throws RestClientException;

  /**
   * Get status for specific simulation
   * 
   * @param simulationName
   * @return
   * @throws RestClientException
   */
  SimulationStatusCode getSimulationStatus(String simulationName) throws RestClientException;


}
