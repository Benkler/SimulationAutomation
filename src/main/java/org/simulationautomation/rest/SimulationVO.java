package org.simulationautomation.rest;

import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.simulation.SimulationStatusCode;

public class SimulationVO {

  private String simulationName;
  private SimulationStatusCode simulationStatus;
  private String creationTimeStamp;


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

  public String getCreationTimeStamp() {
    return creationTimeStamp;
  }

  public void setCreationTimeStamp(String creationTimeStamp) {
    this.creationTimeStamp = creationTimeStamp;
  }


  @Override
  public String toString() {
    return "Simulation:\n" + "    name= " + simulationName + "\n   status= "
        + simulationStatus.getStatus() + "\n    Creation Time Stamp= " + creationTimeStamp;
  }

  public static SimulationVO toSimulationVO(Simulation simulation) {
    SimulationVO simuVO = new SimulationVO();
    simuVO.setSimulationName(simulation.getMetadata().getName());
    simuVO.setSimulationStatus(simulation.getStatus().getStatusCode());
    simuVO.setCreationTimeStamp(simulation.getMetadata().getCreationTimestamp());
    return simuVO;
  }



}
