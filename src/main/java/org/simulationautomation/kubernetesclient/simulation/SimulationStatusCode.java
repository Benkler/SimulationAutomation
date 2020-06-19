package org.simulationautomation.kubernetesclient.simulation;

public enum SimulationStatusCode {

  CREATING("CREATING"), CREATED("CREATED"), RUNNING("RUNNING"), SUCCEEDED("SUCCEEDED"), FAILED(
      "FAILED");


  private String status;

  private SimulationStatusCode(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }
}
