package org.simulationautomation.kubernetesclient.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.simulationautomation.kubernetesclient.api.ISimulationAutomationServiceProxy;
import org.simulationautomation.kubernetesclient.api.ISimulationServiceRegistry;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.rest.SimulationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class as Proxy between REST-interface and actual Backend for operations concerning the
 * simulation automation application.
 * 
 * @author Niko Benkler
 *
 */
@Service
public class SimulationAutomationServiceProxy implements ISimulationAutomationServiceProxy {

  @Autowired
  ISimulationServiceRegistry simulationServiceRegistry;

  @Override
  public List<String> getNamesOfExistingSimulations() {
    return simulationServiceRegistry.getSimulations().stream()
        .map(simulation -> simulation.getMetadata().getName()).collect(Collectors.toList());
  }

  @Override
  public List<SimulationVO> getExistingSimulations() {
    List<Simulation> existingSimulations = simulationServiceRegistry.getSimulations();
    List<SimulationVO> existingSimulationDTOs = new ArrayList<SimulationVO>();

    for (Simulation simulation : existingSimulations) {
      SimulationVO simuDTO = new SimulationVO();
      simuDTO.setSimulationName(simulation.getMetadata().getName());
      simuDTO.setSimulationStatus(simulation.getStatus().getStatusCode());
      existingSimulationDTOs.add(simuDTO);
    }


    return existingSimulationDTOs;
  }

}
