package org.simulationautomation.kubernetesclient.simulation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
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
@Component(value = "simulationService")
public class SimulationService {
  private Logger log = LoggerFactory.getLogger(SimulationService.class);
  private Map<String, Simulation> simulations = new ConcurrentHashMap<>();


  public List<String> getSimulations() {
    log.info("Query all simulations in simulationService");
    return simulations.values().stream().map(a -> a.getMetadata().getName())
        .collect(Collectors.toList());
  }

  public void addSimulation(String simulationName, Simulation simulation) {
    log.info("Add simulation to simulationService with name= " + simulationName);
    simulations.put(simulationName, simulation);

  }

  public Simulation removeSimulation(String simulationName) {
    log.info("Remove simulation from simulationService with name= " + simulationName);
    return simulations.remove(simulationName);
  }

  public Simulation getSimulation(String simulationName) {
    return simulations.get(simulationName);
  }


  public Map<String, Simulation> getSimulationsMap() {
    return simulations;
  }



  public void updateStatus(String simulationName, SimulationStatusCode simulationSatusCode) {
    Simulation simulation = getSimulation(simulationName);
    simulation.getStatus().setStatus(simulationSatusCode);
    simulations.put(simulationName, simulation);


    if (simulationSatusCode == SimulationStatusCode.FAILED) {
      log.info("Update status for Simulation with name=" + simulation.getMetadata().getName()
          + " .Simulation failed!");
    }

    if (simulationSatusCode == SimulationStatusCode.SUCCEEDED) {
      log.info("Update status for Simulation with name=" + simulation.getMetadata().getName()
          + " .Simulation succeded!");
    }

    if (simulationSatusCode == SimulationStatusCode.RUNNING) {
      log.info("Update status for Simulation with name=" + simulation.getMetadata().getName()
          + " .Simulation is running!");
    }

    if (simulationSatusCode == SimulationStatusCode.CREATING) {
      log.info("Update status for Simulation with name=" + simulation.getMetadata().getName()
          + " .Simulation is created!");
    }


  }

  public SimulationStatus getSimulationStatus(String simulationName) {
    return getSimulation(simulationName).getStatus();
  }


}
