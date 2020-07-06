package org.simulationautomation.kubernetesclient.simulation;

import java.util.List;
import org.simulationautomation.kubernetesclient.api.ISimulationLoader;
import org.simulationautomation.kubernetesclient.api.ISimulationServiceRegistry;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.crds.SimulationDoneable;
import org.simulationautomation.kubernetesclient.crds.SimulationList;
import org.simulationautomation.kubernetesclient.crds.SimulationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

/**
 * This class manages the current Simulations
 *
 * @author Niko Benkler
 *
 */
// TODO HashMap rausschmeißen
@Component
public class SimulationServiceRegistry implements ISimulationServiceRegistry {
  private Logger log = LoggerFactory.getLogger(SimulationServiceRegistry.class);

  @Autowired
  private NonNamespaceOperation<Simulation, SimulationList, SimulationDoneable, Resource<Simulation, SimulationDoneable>> simulationCRDClient;


  @Autowired
  private ISimulationLoader simulationLoader;



  /**
   * Query all simulations currently stored in this service
   * 
   * @return
   */
  @Override
  public List<Simulation> getSimulations() {
    log.info("Query all simulations in namespace");
    return simulationCRDClient.list().getItems();
  }



  @Override
  public Simulation getSimulation(String simulationName) {
    log.info("Query simulation with name=" + simulationName);
    if (simulationName == null) {
      log.info("Could not query simulation as given name was null");
      return null;
    }
    List<Simulation> simulations = simulationCRDClient.list().getItems();

    for (Simulation simulation : simulations) {
      if (simulation.getMetadata().getName().equals(simulationName)) {
        log.info("Returning simulation with name=" + simulationName);
        return simulation;
      }
    }
    log.info("No Simulation found for name=" + simulationName);
    return null;
  }


  /**
   * Update given simulation status for simulation with given name
   * 
   * @param simulationName
   * @param simulationSatusCode
   */
  @Override
  public void updateStatus(Simulation simulation, SimulationStatusCode simulationSatusCode) {
    if (simulation == null) {
      log.error("Provided simulation is null!");
    }

    String simulationName = simulation.getMetadata().getName();

    log.info("Trying to update simulation with name=" + simulationName);


    if (simulation.getStatus() == null) {
      log.error("Status for simulation with name=" + simulationName
          + " is null. This should not have happened");
      return;
    }

    simulation.getStatus().setStatus(simulationSatusCode);
    log.info("New status is: " + simulation.getStatus().toString());

    // Update status with updateStatus() method does somehow not work
    // simulationCRDClient.updateStatus(simulation);



    /*
     * Write metadata to file system
     */
    simulationLoader.updateSimulationMetadata(simulation);



    if (simulationSatusCode == SimulationStatusCode.FAILED) {
      log.info("Update status for Simulation with name=" + simulation.getMetadata().getName()
          + ". Simulation failed!");
    }

    if (simulationSatusCode == SimulationStatusCode.SUCCEEDED) {
      log.info("Update status for Simulation with name=" + simulation.getMetadata().getName()
          + ". Simulation succeded!");
    }

    if (simulationSatusCode == SimulationStatusCode.RUNNING) {
      log.info("Update status for Simulation with name=" + simulation.getMetadata().getName()
          + ". Simulation is running!");
    }

    if (simulationSatusCode == SimulationStatusCode.CREATED) {
      log.info("Update status for Simulation with name=" + simulation.getMetadata().getName()
          + ". Simulation is created!");
    }


  }

  @Override
  public SimulationStatus getSimulationStatus(String simulationName) {
    Simulation simulation = getSimulation(simulationName);
    if (simulation == null) {
      log.error("Could not retrieve simulation status for simulation with name= " + simulationName
          + ". Simulation not found");
      return null;
    } else {
      return simulation.getStatus();
    }


  }


}
