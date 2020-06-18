package org.simulationautomation.kubernetesclient.operator;

import static org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties.SIMULATION_GROUP;
import static org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties.SIMULATION_KIND;
import static org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties.SIMULATION_NAMESPACE;
import java.util.List;
import org.simulationautomation.kubernetesclient.api.ICustomNameSpaceBuilder;
import org.simulationautomation.kubernetesclient.api.IK8SCoreRuntime;
import org.simulationautomation.kubernetesclient.api.ISimulationFactory;
import org.simulationautomation.kubernetesclient.api.ISimulationOperator;
import org.simulationautomation.kubernetesclient.api.ISimulationPodFactory;
import org.simulationautomation.kubernetesclient.api.ISimulationPodWatcher;
import org.simulationautomation.kubernetesclient.api.ISimulationService;
import org.simulationautomation.kubernetesclient.api.ISimulationWatcher;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.crds.SimulationDoneable;
import org.simulationautomation.kubernetesclient.crds.SimulationList;
import org.simulationautomation.kubernetesclient.exceptions.SimulationCreationException;
import org.simulationautomation.kubernetesclient.simulation.SimulationStatusCode;
import org.simulationautomation.kubernetesclient.util.SimulationCRDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
  private ISimulationService simulationsService;

  @Autowired
  private ISimulationWatcher simulationWatcher;

  @Autowired
  private ISimulationPodWatcher simulationPodWatcher;

  @Autowired
  private IK8SCoreRuntime k8SCoreRuntime;

  @Autowired
  ICustomNameSpaceBuilder nsBuilder;

  @Autowired
  ISimulationPodFactory simulationPodFactory;

  @Autowired
  ISimulationFactory simulationFactory;

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
    registerSimulationWatchers();

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

  /**
   * Create a simulation custom resource with given name in simulation namespace. </br>
   * 
   * @param name
   * @throws SimulationCreationException
   */
  // TODO further parameters necessary
  @Override
  public Simulation createSimulation() throws SimulationCreationException {



    Simulation createdSimulation;
    log.info("Trying to prepare simulation.");
    createdSimulation = simulationFactory.createAndPrepareSimulation();
    log.info(
        "Successfully prepared simulation with id=" + createdSimulation.getMetadata().getName());
    Simulation persistedSimulation = simulationCRDClient.createOrReplace(createdSimulation);
    simulationsService.addSimulation(persistedSimulation.getMetadata().getName(),
        persistedSimulation);

    addSimulationPod(persistedSimulation);
    log.info("Successfully persisted simulation: " + persistedSimulation);


    simulationsService.updateStatus(persistedSimulation.getMetadata().getName(),
        SimulationStatusCode.RUNNING);

    return persistedSimulation;

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



  /**
   * 
   * @param simulation
   */
  private void addSimulationPod(Simulation simulation) {
    log.info("Trying to add pod with name=" + simulation.getMetadata().getName() + " in namespace="
        + SIMULATION_NAMESPACE);
    Pod persistedPod = simulationPodFactory.createSimulationPod(simulation);
    client.pods().inNamespace(SIMULATION_NAMESPACE).create(persistedPod);

  }



}
