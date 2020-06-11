package org.simulationautomation.kubernetesclient.simulation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.simulationautomation.kubernetesclient.crds.Simulation;
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
  // TODO necessary?
  private Map<String, String> simulationURLs = new HashMap<>();

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

  public String getSimulationUrl(String simulationName) {
    return simulationURLs.get(simulationName);
  }

  // TODO necessary?
  public void addSimulationUrl(String simulationName, String url) {
    simulationURLs.put(simulationName, url);
  }

  public Map<String, Simulation> getSimulationsMap() {
    return simulations;
  }

  public Map<String, String> getSimulationsUrls() {
    return simulationURLs;
  }
}
