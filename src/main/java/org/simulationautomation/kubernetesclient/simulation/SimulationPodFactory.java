package org.simulationautomation.kubernetesclient.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.simulationautomation.kubernetesclient.api.ISimulationPodFactory;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.simulation.properties.SimulationPathFactory;
import org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.NFSVolumeSource;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeMount;

/**
 * Factory class to create pods that can execute a simulation
 * 
 * @author Niko Benkler
 *
 */
@Service
public class SimulationPodFactory implements ISimulationPodFactory {
  private static final String POD_RESTART_POLICY = "Never";
  private static final String IMAGE_PULL_POLICY = "IfNotPresent";
  private static final String NFS_INPUT_NAME = "input-nfs";
  private static final String NFS_OUTPUT_NAME = "output-nfs";
  private static final String PALLADIO_CONTAINER_NAME = "palladiosimulation";
  private static final String GENERATED_EXPERIMENTS_FILE_NAME = "Generated.experiments";

  private static final Logger log = LoggerFactory.getLogger(SimulationPodFactory.class);

  @Override
  public Pod createSimulationPod(Simulation simulation) {
    String simulationName = simulation.getMetadata().getName();

    log.info("Create Pod from Simulation with name= " + simulationName);

    /*
     * Name of Volume here specifies the volume mount during container creation in
     * createVolumeMount()
     */
    Volume inputVolume = createNFSVolumeDefinition(NFS_INPUT_NAME, simulationName,
        SimulationProperties.SIMULATION_INPUT_FOLDER_NAME);
    Volume outpuVolume = createNFSVolumeDefinition(NFS_OUTPUT_NAME, simulationName,
        SimulationProperties.SIMULATION_OUTPUT_FOLDER_NAME);

    Container container = createSimulationContainer(simulation);

    Pod pod = new PodBuilder().withApiVersion("v1")

        .withNewMetadata().withGenerateName(simulation.getMetadata().getName() + "-pod-")
        .withNamespace(simulation.getMetadata().getNamespace())
        .withLabels(Collections.singletonMap(SimulationProperties.SIMULATION_LABEL,
            simulation.getMetadata().getName()))

        .addNewOwnerReference().withController(true).withKind(SimulationProperties.SIMULATION_KIND)
        .withApiVersion("demo.k8s.io/v1alpha1").withName(simulation.getMetadata().getName())
        .withNewUid(simulation.getMetadata().getUid()).endOwnerReference()

        .endMetadata()

        .withNewSpec().withVolumes(inputVolume, outpuVolume).withContainers(container)
        .withRestartPolicy(POD_RESTART_POLICY).endSpec().build();

    return pod;
  }

  private Container createSimulationContainer(Simulation simulation) {
    log.info("Create simulation container for simulation with name= "
        + simulation.getMetadata().getName());

    VolumeMount inputVolumeMount =
        createVolumeMount(NFS_INPUT_NAME, SimulationProperties.PALLADIO_IMAGE_INPUT_MOUNT_PATH);

    VolumeMount outputVolumeMount =
        createVolumeMount(NFS_OUTPUT_NAME, SimulationProperties.PALLADIO_IMAGE_OUTPUT_MOUNT_PATH);

    List<String> commands = new ArrayList<>();
    commands.add(SimulationProperties.PALLADIO_IMAGE_ENTRY_SCRIPT_PATH);

    List<String> args = new ArrayList<>();

    // Get path to .experiments file
    String pathToExperimentsFile = SimulationPathFactory
        .getPathToSimulationExperimentFileInContainer(simulation.getSpec().getSimulationFileName());
    String pathToGeneratedFile = createPathToGeneratedFile(pathToExperimentsFile);
    log.info("Path argument for experiments file= " + pathToExperimentsFile);
    log.info("Path argument for generated file= " + pathToGeneratedFile);
    args.add(pathToExperimentsFile);
    args.add(pathToGeneratedFile);
    return new ContainerBuilder().withName(PALLADIO_CONTAINER_NAME)
        .withImage(SimulationProperties.PALLADIO_IMAGE).withImagePullPolicy(IMAGE_PULL_POLICY)
        .withVolumeMounts(inputVolumeMount, outputVolumeMount).addAllToCommand(commands)
        .addAllToArgs(args).build();
  }

  /*
   * Generated.experiments file needs to be created in same directory than actual .experiments File
   * Example: /dir/temp.experiments -> /dir/Generated.experiments
   */
  private String createPathToGeneratedFile(String pathToExperimentsFile) {

    int index = pathToExperimentsFile.lastIndexOf("/");
    if (index == -1) {
      return GENERATED_EXPERIMENTS_FILE_NAME;
    } else {
      return pathToExperimentsFile.substring(0, index) + "/" + GENERATED_EXPERIMENTS_FILE_NAME;
    }

  }

  /**
   * Create Volume Definition for Pod: </br>
   * The path needs to match an existing folder, which was created by the simulation operator
   * beforehand. </br>
   * Path consists of "/simulationId/{Input|Output}/" </br>
   * 
   * specifying the path of an existing folder in the NFS and the name
   * 
   * @param simulationId
   * @param nfsName
   * @param folderName
   * @return
   */
  private Volume createNFSVolumeDefinition(String nfsName, String simulationId, String folderName) {

    log.info("Create NFS Volume Definition for simulation with name= " + simulationId);
    NFSVolumeSource nfsVolumeSource = new NFSVolumeSource();
    nfsVolumeSource.setPath(
        SimulationPathFactory.getPathToSpecificFolderWithinSimulation(simulationId, folderName));
    nfsVolumeSource.setServer(SimulationProperties.NFS_SERVER_IP);
    Volume nfs_volume = new Volume();
    nfs_volume.setName(nfsName);
    nfs_volume.setNfs(nfsVolumeSource);

    return nfs_volume;
  }

  /**
   * Create VolumeMount for Docker Container
   * 
   * @param nfsName, specified in the @Volume
   * @param mountPath, where to mount within Container
   * @return
   */
  private VolumeMount createVolumeMount(String nfsName, String mountPath) {
    VolumeMount volumeMount = new VolumeMount();
    volumeMount.setName(nfsName);
    volumeMount.setMountPath(mountPath);
    return volumeMount;
  }

}
