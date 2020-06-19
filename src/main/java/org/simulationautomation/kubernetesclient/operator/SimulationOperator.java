package org.simulationautomation.kubernetesclient.operator;

import static org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties.SIMULATION_GROUP;
import static org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties.SIMULATION_KIND;
import static org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties.SIMULATION_NAMESPACE;
import java.util.List;
import org.simulationautomation.kubernetesclient.api.ICustomNameSpaceBuilder;
import org.simulationautomation.kubernetesclient.api.IK8SCoreRuntime;
import org.simulationautomation.kubernetesclient.api.ISimulationFactory;
import org.simulationautomation.kubernetesclient.api.ISimulationLogWatcher;
import org.simulationautomation.kubernetesclient.api.ISimulationOperator;
import org.simulationautomation.kubernetesclient.api.ISimulationPodFactory;
import org.simulationautomation.kubernetesclient.api.ISimulationPodWatcher;
import org.simulationautomation.kubernetesclient.api.ISimulationServiceRegistry;
import org.simulationautomation.kubernetesclient.api.ISimulationWatcher;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.crds.SimulationDoneable;
import org.simulationautomation.kubernetesclient.crds.SimulationList;
import org.simulationautomation.kubernetesclient.exceptions.SimulationCreationException;
import org.simulationautomation.kubernetesclient.simulation.SimulationStatusCode;
import org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties;
import org.simulationautomation.kubernetesclient.util.SimulationCRDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

@Component
public class SimulationOperator implements ISimulationOperator {

  static final String IMAGE_PULL_POLICY = "IfNotPresent";
  static final String POD_RESTART_POLICY = "Never";



  private Logger log = LoggerFactory.getLogger(SimulationOperator.class);

  private CustomResourceDefinition simulationCRD = null;

  // private String simulationResourceVersion;

  private NonNamespaceOperation<Simulation, SimulationList, SimulationDoneable, Resource<Simulation, SimulationDoneable>> simulationCRDClient;

  @Autowired
  private KubernetesClient client;

  @Autowired
  private ISimulationServiceRegistry simulationsServiceRegistry;

  @Autowired
  private ISimulationWatcher simulationWatcher;

  @Autowired
  private ISimulationPodWatcher simulationPodWatcher;

  @Autowired
  private ISimulationLogWatcher simulationLogWatcher;

  @Autowired
  private IK8SCoreRuntime k8SCoreRuntime;

  @Autowired
  private ICustomNameSpaceBuilder nsBuilder;

  @Autowired
  private ISimulationPodFactory simulationPodFactory;

  @Autowired
  private ISimulationFactory simulationFactory;

  /**
   * Init all necessary fields, including simulation namespace, simulation resource definition and
   * watcher for simulation resource definitions and simulation pods
   */
  @Override
  public void init() {
    nsBuilder.createNamespace(SIMULATION_NAMESPACE);
    simulationCRD = createAndRegisterSimulationCRD();
    // Creating CRD Client for simulation
    simulationCRDClient = k8SCoreRuntime.customResourcesClientInNameSpace(simulationCRD,
        Simulation.class, SimulationList.class, SimulationDoneable.class, SIMULATION_NAMESPACE);
    // Delete simulations in namespace
    deleteExistingSimulationsResources();

    // Register watcher for both, simulations (crds) and pods in namespace simulation
    registerSimulationWatcher();
    registerSimulationPodWatcher();

  }

  /**
   * List custom Resources for type Simulation in given namepsace
   * 
   * @param namespace
   * @return
   * @return
   */
  @Override
  public List<Simulation> listExistingSimulations() {
    return simulationCRDClient.list().getItems();
  }

  @Override
  public Pod getPodBySimulationName(String simulationName) {

    List<Pod> podList =
        client.pods().inNamespace(SimulationProperties.SIMULATION_NAMESPACE).list().getItems();

    for (Pod pod : podList) {
      OwnerReference or = getControllerOfPod(pod);
      if (or == null) {
        continue;
      }

      if (simulationName.equals(or.getName())) {
        return pod;
      }
    }

    return null;
  }

  /**
   * Create a simulation custom resource with given name in simulation namespace. </br>
   * 
   * @param name
   * @throws SimulationCreationException
   */
  // TODO further parameters necessary
  @Override
  public Simulation createSimulation() throws SimulationCreationException {
    log.info("Trying to prepare simulation.");

    // Create Simulation and prepare local folder structure
    Simulation createdSimulation = simulationFactory.createAndPrepareSimulation();
    String simulationName = createdSimulation.getMetadata().getName();
    log.info("Successfully prepared simulation with id=" + simulationName);

    // Add simulation to k8s cluster
    Simulation persistedSimulation = simulationCRDClient.createOrReplace(createdSimulation);
    log.info("Successfully persisted simulation: " + persistedSimulation);

    if (persistedSimulation == null) {
      log.error("Error while creating simulation with name=" + simulationName);
      throw new SimulationCreationException(
          "Error while creating simulation with name=" + simulationName);
    }

    // Add simulation to local service registry
    simulationsServiceRegistry.addSimulation(persistedSimulation.getMetadata().getName(),
        persistedSimulation);

    // Create simulation pod for specified simulation
    Pod persistedSimulationPod = createAndPersistSimulationPod(persistedSimulation);

    if (persistedSimulationPod == null) {
      deleteExistingSimulation(persistedSimulation);
      throw new SimulationCreationException("Could not create simulation with name="
          + persistedSimulation.getMetadata().getName() + ", as no Pod could be instantiated");
    }

    // Register
    registerSimulationPodLogWatcher(persistedSimulation, persistedSimulationPod);


    simulationsServiceRegistry.updateStatus(persistedSimulation.getMetadata().getName(),
        SimulationStatusCode.RUNNING);

    return persistedSimulation;

  }

  /**
   * Try to create simulation
   * 
   * @param simulation
   * @throws SimulationCreationException
   */
  private Pod createAndPersistSimulationPod(Simulation simulation)
      throws SimulationCreationException {
    log.info("Trying to add pod for simulation with name=" + simulation.getMetadata().getName()
        + " in namespace=" + SIMULATION_NAMESPACE);
    Pod createdPod = simulationPodFactory.createSimulationPod(simulation);
    return client.pods().inNamespace(SIMULATION_NAMESPACE).create(createdPod);

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

  private void deleteExistingSimulation(Simulation simulation) {
    log.info("Delete simulation with name=" + simulation.getMetadata().getName());
    simulationCRDClient.delete(simulation);
  }

  private CustomResourceDefinition createAndRegisterSimulationCRD() {

    CustomResourceDefinition simuCRD = SimulationCRDUtil.getCRD();
    k8SCoreRuntime.registerCustomResourceDefinition(simuCRD);
    k8SCoreRuntime.registerCustomKind(SIMULATION_GROUP + "/v1", SIMULATION_KIND, Simulation.class);

    log.info("SimulationCRD successfully registered");
    return simuCRD;
  }

  /*
   * Register Pod Watcher in namespace "simulation".
   */
  private void registerSimulationPodWatcher() {
    log.info("Registering Pod Watcher");
    client.pods().inNamespace(SIMULATION_NAMESPACE).watch(simulationPodWatcher);
  }

  /*
   * Register Simulation Watcher
   */
  private void registerSimulationWatcher() {
    log.info("Registering Simulation Watcher");
    simulationCRDClient.watch(simulationWatcher);


  }

  /*
   * Register LogWatcher for a given pod of a simulation.
   */
  private void registerSimulationPodLogWatcher(Simulation simulation, Pod simulationPod) {
    simulationLogWatcher.registerSimulationPodLogWatcher(simulation, simulationPod);

  }

  /*
   * Pod has a Simulation Owner
   */
  private OwnerReference getControllerOfPod(Pod pod) {
    List<OwnerReference> ownerReferences = pod.getMetadata().getOwnerReferences();
    for (OwnerReference ownerReference : ownerReferences) {
      if (ownerReference.getController()) {
        return ownerReference;
      }
    }
    return null;
  }



}
