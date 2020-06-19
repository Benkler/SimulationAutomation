package org.simulationautomation.kubernetesclient.simulation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.simulationautomation.kubernetesclient.api.ISimulationServiceRegistry;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.crds.SimulationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This class manages the current Simulations
 * 
 * @author Niko Benkler
 *
 */
@Component
public class SimulationServiceRegistry implements ISimulationServiceRegistry {
  private Logger log = LoggerFactory.getLogger(SimulationServiceRegistry.class);
  private Map<String, Simulation> simulations = new ConcurrentHashMap<>();


  /**
   * Query all simulations currently stored in this service
   * 
   * @return
   */
  @Override
  public List<String> getSimulations() {
    log.info("Query all simulations in simulationService");
    return simulations.values().stream().map(a -> a.getMetadata().getName())
        .collect(Collectors.toList());
  }


  @Override
  public void addSimulation(String simulationName, Simulation simulation) {
    log.info("Add simulation to simulation service: " + simulation);
    simulations.put(simulationName, simulation);

  }

  @Override
  public Simulation removeSimulation(String simulationName) {
    log.info("Remove simulation from simulationService with name= " + simulationName);
    return simulations.remove(simulationName);
  }

  @Override
  public Simulation getSimulation(String simulationName) {
    return simulations.get(simulationName);
  }


  @Override
  public Map<String, Simulation> getSimulationsMap() {
    return simulations;
  }


  /**
   * Update given simulation status for simulation with given name
   * 
   * @param simulationName
   * @param simulationSatusCode
   */
  @Override
  public void updateStatus(String simulationName, SimulationStatusCode simulationSatusCode) {
    Simulation simulation = getSimulation(simulationName);

    if (simulation.getStatus() == null) {
      log.error("Status for simulation with name=" + simulationName
          + " is null. This should not have happened");
      return;
    }

    simulation.getStatus().setStatus(simulationSatusCode);
    simulations.put(simulationName, simulation);


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

    if (simulationSatusCode == SimulationStatusCode.CREATING) {
      log.info("Update status for Simulation with name=" + simulation.getMetadata().getName()
          + ". Simulation is created!");
    }


  }

  @Override
  public SimulationStatus getSimulationStatus(String simulatioUUID) {
    return getSimulation(simulatioUUID).getStatus();
  }


}
