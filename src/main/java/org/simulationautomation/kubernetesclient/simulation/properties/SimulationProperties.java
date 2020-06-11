
package org.simulationautomation.kubernetesclient.simulation.properties;

public final class SimulationProperties {
  /*
   * Properties mainly used to create and manage Simulation CRD
   */
  public static final String SIMULATION_GROUP = "palladio.org";
  public static final String SIMULATION_NAME = "simulator";
  public static final String SIMULATION_NAME_PLURAL = "simulators";
  public static final String SIMULATION_KIND = "Simulation";
  public static final String SIMULATION_SCOPE = "Namespaced";
  public static final String SIMULATION_SHORT_NAME = "sim";
  public static final String SIMULATION_CRD_NAME = SIMULATION_NAME_PLURAL + "." + SIMULATION_GROUP;

  /*
   * Properties used to manage simulation environment
   */
  public static final String SIMULATION_NAMESPACE = "simulations";
  public static final String SIMULATION_LABEL = "sim";

  /*
   * Properties used to manage folder structure
   */
  public static final String SIMULATION_BASE_PATH = "/usr/Simulation";
  public static final String SIMULATION_INPUT_FOLDER_NAME = "Input";
  public static final String SIMULATION_OUTPUT_FOLDER_NAME = "Output";
  // TODO remove as soon as REST API is finished
  public static final String SIMULATION_EXPERIMENT_FILES_PATH =
      "/usr/ExperimentFiles/ExperimentData";

  /*
   * Properties used to manage NFS File Structure
   */
  public static final String NFS_SERVER_IP = "10.100.224.55";


  /*
   * Properties used for palladio-experimet automation image
   */

  public static final String PALLADIO_IMAGE = "palladiosimulator/palladio-experimentautomation";
  public static final String PALLADIO_IMAGE_ENTRY_SCRIPT_PATH = "/usr/RunExperimentAutomation.sh";
  public static final String PALLADIO_IMAGE_INPUT_MOUNT_PATH = "/usr/ExperimentData";
  public static final String PALLADIO_IMAGE_OUTPUT_MOUNT_PATH = "/result";
}

