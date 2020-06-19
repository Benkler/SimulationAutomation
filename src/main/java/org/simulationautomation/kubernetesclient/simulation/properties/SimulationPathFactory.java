package org.simulationautomation.kubernetesclient.simulation.properties;

public class SimulationPathFactory {


  /**
   * 
   * @param simulationName
   * @return {SimulationProperties.SIMULATION_BASE_PATH}/{simulationName}/{SimulationProperties.SIMULATION_LOG_FILE_NAME}
   */
  public static String getPathToSimulationLogFile(String simulationName) {
    return SimulationProperties.SIMULATION_BASE_PATH + "/" + simulationName + "/"
        + SimulationProperties.SIMULATION_LOG_FILE_NAME;
  }

  public static String getPathToSimulationMetadataFile(String simulationName) {
    return SimulationProperties.SIMULATION_BASE_PATH + "/" + simulationName + "/"
        + SimulationProperties.SIMULATION_METADATA_FILE_NAME;
  }



  /**
   * 
   * @param simulationName
   * @param folderName
   * @return /{simulationName}/{folderName}
   */
  public static String getPathToSpecificFolderWithinSimulation(String simulationName,
      String folderName) {
    return "/" + simulationName + "/" + folderName + "/";
  }

  /**
   * 
   * @param simulationName
   * @return {SimulationProperties.SIMULATION_BASE_PATH}/{simulationName}/{SimulationProperties.SIMULATION_INPUT_FOLDER_NAME}
   * 
   */
  public static String getPathToInputFolderOfSimulation(String simulationName) {
    return SimulationProperties.SIMULATION_BASE_PATH + "/" + simulationName + "/"
        + SimulationProperties.SIMULATION_INPUT_FOLDER_NAME;
  }

  /**
   * 
   * @param simulationName
   * @return {SimulationProperties.SIMULATION_BASE_PATH}/{simulationName}/{SimulationProperties.SIMULATION_OUTPUT_FOLDER_NAME}
   * 
   */
  public static String getPathToOutputFolderOfSimulation(String simulationName) {
    return SimulationProperties.SIMULATION_BASE_PATH + "/" + simulationName + "/"
        + SimulationProperties.SIMULATION_OUTPUT_FOLDER_NAME;
  }


}