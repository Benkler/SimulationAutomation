package org.simulationautomation.kubernetesclient.simulation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.simulationautomation.kubernetesclient.crds.SimulationCR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component(value = "simulationService")
public class SimulationService {
	private Logger logger = LoggerFactory.getLogger(SimulationService.class);
	private Map<String, SimulationCR> simulations = new ConcurrentHashMap<>();
	private Map<String, String> simulationURLs = new HashMap<>();

	public List<String> getSimulations() {
		return simulations.values().stream().map(a -> a.getMetadata().getName()).collect(Collectors.toList());
	}

	public void addSimulation(String simulationName, SimulationCR simulation) {
		simulations.put(simulationName, simulation);
	}

	public SimulationCR removeSimulation(String simulationName) {
		return simulations.remove(simulationName);
	}

	public SimulationCR getSimulation(String simulationName) {
		return simulations.get(simulationName);
	}

	public String getSimulationUrl(String simulationName) {
		return simulationURLs.get(simulationName);
	}

	public void addSimulationUrl(String simulationName, String url) {
		simulationURLs.put(simulationName, url);
	}

	public Map<String, SimulationCR> getSimulationsMap() {
		return simulations;
	}

	public Map<String, String> getSimulationsUrls() {
		return simulationURLs;
	}
}
