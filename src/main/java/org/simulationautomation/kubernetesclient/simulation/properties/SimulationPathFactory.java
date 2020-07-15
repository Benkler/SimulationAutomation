package org.simulationautomation.kubernetesclient.simulation.properties;

public class SimulationPathFactory {

  private static SimulationPathFactory INSTANCE;

  private SimulationPathFactory() {};

  // Singleton
  public static synchronized SimulationPathFactory getInstance() {
    if (INSTANCE == null) {
      SimulationPathFactory.INSTANCE = new SimulationPathFactory();
    }
    return SimulationPathFactory.INSTANCE;
  }

  public static String getPathToSimulationExperimentFileInContainer(String experimentFileName) {
    return SimulationProperties.PALLADIO_IMAGE_INPUT_MOUNT_PATH + "/" + experimentFileName;
  }

  /**
   * 
   * @param simulationName
   * @return {SimulationProperties.SIMULATION_BASE_PATH}/{simulationName}
   */
  public static String getPathToSimulationFolder(String simulationName) {
    return SimulationProperties.SIMULATION_BASE_PATH + "/" + simulationName;
  }


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


  public static String getPathToFileInInputFolderOfSimulation(String simulationName,
      String fileName) {
    return getPathToInputFolderOfSimulation(simulationName) + "/" + fileName;
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

  public static String getPathToZipFileOfSImulation(String simulationName) {

    return SimulationProperties.SIMULATION_BASE_PATH + "/" + simulationName + "/"
        + SimulationProperties.SIMULATION_RESULT_ZIP_NAME;
  }


}
