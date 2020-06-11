package org.simulationautomation.kubernetesclient.simulation;

import org.simulationautomation.kubernetesclient.crds.SimulationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SimulationServiceProxy {

  @Autowired
  SimulationService simulationService;


  public boolean isSimulationFinished(String simulationName) {

    SimulationStatus status = simulationService.getSimulationStatus(simulationName);

    return status.getStatus().equals(SimulationStatusCode.SUCCEEDED);

  }


  public boolean doesSimulationExist(String simulationName) {
    return simulationService.getSimulation(simulationName) != null;
  }



}
