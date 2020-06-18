package org.simulationautomation.kubernetesclient.api;



public interface ISimulationServiceProxy {

  boolean isSimulationFinished(String simulationName);

  boolean doesSimulationExist(String simulationName);

  /**
   * Get zipped simulation results as byte array
   * 
   * @param simulationName
   * @return
   */
  byte[] getSimulationResults(String simulationName);

  String getSimulationLog(String simulationName);

}
