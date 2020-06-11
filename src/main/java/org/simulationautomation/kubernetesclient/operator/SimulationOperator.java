package org.simulationautomation.kubernetesclient.operator;

import static org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties.SIMULATION_GROUP;
import static org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties.SIMULATION_KIND;
import static org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties.SIMULATION_NAMESPACE;
import java.util.List;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.crds.SimulationDoneable;
import org.simulationautomation.kubernetesclient.crds.SimulationList;
import org.simulationautomation.kubernetesclient.simulation.SimulationPodCreator;
import org.simulationautomation.kubernetesclient.simulation.SimulationService;
import org.simulationautomation.kubernetesclient.util.CustomNamespaceBuilder;
import org.simulationautomation.kubernetesclient.util.SimulationCRDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.fabric8.kubernetes.api.builder.Predicate;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

@Component
public class SimulationOperator {

  static final String IMAGE_PULL_POLICY = "IfNotPresent";
  static final String POD_RESTART_POLICY = "Never";
  static final String POD_PHASE_SUCCEEDED = "Succeeded";
  static final String POD_PHASE_FAILED = "Failed";
  private static final JobFinishedPredicate POD_PREDICATE = new JobFinishedPredicate();

  private Logger log = LoggerFactory.getLogger(SimulationOperator.class);

  private CustomResourceDefinition simulationCRD = null;

  // private String simulationResourceVersion;

  private NonNamespaceOperation<Simulation, SimulationList, SimulationDoneable, Resource<Simulation, SimulationDoneable>> simulationCRDClient;

  @Autowired
  private KubernetesClient client;

  @Autowired
  private SimulationService simulationsService;

  @Autowired
  private SimulationWatcher simulationWatcher;

  @Autowired
  private SimulationPodWatcher simulationPodWatcher;

  @Autowired
  private K8SCoreRuntime k8SCoreRuntime;

  @Autowired
  CustomNamespaceBuilder nsBuilder;

  /**
   * Init all necessary fields, including simulation namespace, simulation resource definition and
   * watcher for simulation resource definitions and simulation pods
   */
  public void init() {
    nsBuilder.createNamespace(SIMULATION_NAMESPACE);
    simulationCRD = createAndRegisterSimulationCRD();
    // Creating CRD Client for simulation
    simulationCRDClient = k8SCoreRuntime.customResourcesClientInNameSpace(simulationCRD,
        Simulation.class, SimulationList.class, SimulationDoneable.class, SIMULATION_NAMESPACE);
    // Delete simulations in namespace
    deleteExistingSimulationsResources();
    registerSimulationWatchers();

  }

  /**
   * List custom Resources for type Simulation in given namepsace
   * 
   * @param namespace
   * @return
   * @return
   */
  public List<Simulation> listExistingSimulations() {
    return simulationCRDClient.list().getItems();
  }

  /**
   * Create a simulation custom resource with given name in simulation namespace. </br>
   * 
   * @param name
   */
  // TODO further parameters necessary
  public void createSimulation(String name) {

    log.info("Trying to create simulation with name=" + name);
    Simulation simuCr =
        simulationCRDClient.createOrReplace(createSimulationCR(name, SIMULATION_NAMESPACE));

    simulationsService.addSimulation(simuCr.getMetadata().getName(), simuCr);

    addSimulationPod(simuCr);
    log.info("Successfully added simulation with name=" + name);

  }

  private CustomResourceDefinition createAndRegisterSimulationCRD() {

    CustomResourceDefinition simuCRD = SimulationCRDUtil.getCRD();
    k8SCoreRuntime.registerCustomResourceDefinition(simuCRD);
    k8SCoreRuntime.registerCustomKind(SIMULATION_GROUP + "/v1", SIMULATION_KIND, Simulation.class);

    log.info("SimulationCRD successfully registered");
    return simuCRD;
  }

  /*
   * Delete existing resources after startup to avoid clashes in resource version
   */
  private void deleteExistingSimulationsResources() {
    // Load Existing Simulations
    List<Simulation> simulationList = simulationCRDClient.list().getItems();
    log.info("Amount of removed simulations: " + simulationList.size());
    simulationCRDClient.delete(simulationList);

  }

  /*
   * Register Simulation Watch - This watcher is in charge of SimulationCRs and its Pods
   */
  private void registerSimulationWatchers() {
    log.info("Registering CRD Watch");
    simulationCRDClient.watch(simulationWatcher);

    log.info("Registering Pod Watch");
    client.pods().inNamespace(SIMULATION_NAMESPACE).watch(simulationPodWatcher);

  }

  private Simulation createSimulationCR(String name, String nameSpace) {
    Simulation simuCR = new Simulation();
    ObjectMeta metaData = new ObjectMeta();
    metaData.setName(name);
    metaData.setNamespace(nameSpace);
    simuCR.setMetadata(metaData);

    // TODO set further spec

    return simuCR;
  }

  /**
   * 
   * @param simulation
   */
  private void addSimulationPod(Simulation simulation) {
    log.info("Trying to add pod with name=" + simulation.getMetadata().getName() + " in namespace="
        + SIMULATION_NAMESPACE);
    Pod pod = SimulationPodCreator.createSimulationPod(simulation);
    client.pods().inNamespace(SIMULATION_NAMESPACE).create(pod);

  }

  /**
   * Checks whether pod is Failed or Successfully finished command execution
   */
  static class JobFinishedPredicate implements Predicate<Pod> {
    @Override
    public Boolean apply(Pod pod) {
      if (pod.getStatus() == null) {
        return false;
      }
      switch (pod.getStatus().getPhase()) {
        case POD_PHASE_FAILED:
          // fall through
        case POD_PHASE_SUCCEEDED:
          // job is finished.
          return true;
        default:
          // job is not finished.
          return false;
      }
    }
  }

  // void execute(String workspaceId, String[] commandBase, String...
  // arguments) {
  // final String jobName = commandBase[0];
  // final String podName = jobName + '-' + workspaceId;
  // final String[] command = buildCommand(commandBase, arguments);
  // final Pod pod = newPod(podName, command);
  // OpenShiftPods pods = null;
  // try {
  // pods = factory.create(workspaceId).pods();
  // pods.create(pod);
  // final Pod finished = pods.wait(podName, WAIT_POD_TIMEOUT_MIN,
  // POD_PREDICATE::apply);
  // if (POD_PHASE_FAILED.equals(finished.getStatus().getPhase())) {
  // LOG.error("Job command '%s' execution is failed.",
  // Arrays.toString(command));
  // }
  // } catch (InfrastructureException ex) {
  // LOG.error(
  // "Unable to perform '{}' command for the workspace '{}' cause: '{}'",
  // Arrays.toString(command),
  // workspaceId,
  // ex.getMessage());
  // } finally {
  // if (pods != null) {
  // try {
  // pods.delete(podName);
  // } catch (InfrastructureException ignored) {
  // }
  // }
  // }
  // }

}
