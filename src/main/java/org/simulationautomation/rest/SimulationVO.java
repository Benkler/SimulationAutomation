package org.simulationautomation.rest;

import org.simulationautomation.kubernetesclient.simulation.SimulationStatusCode;

public class SimulationVO {

  private String simulationName;
  private SimulationStatusCode simulationStatus;


  public SimulationStatusCode getSimulationStatus() {
    return simulationStatus;
  }

  public void setSimulationStatus(SimulationStatusCode simulationStatus) {
    this.simulationStatus = simulationStatus;
  }

  public String getSimulationName() {
    return simulationName;
  }

  public void setSimulationName(String simulationName) {
    this.simulationName = simulationName;
  }

  @Override
  public String toString() {
    return "Simulation:\n" + "    name= " + simulationName + "\n   status= "
        + simulationStatus.getStatus();
  }


}
