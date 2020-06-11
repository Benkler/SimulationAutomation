package org.simulationautomation.kubernetesclient.exceptions;

public class SimulationCreationException extends Exception {

  public SimulationCreationException(String errorMessage, Throwable err) {
    super(errorMessage, err);
  }

}
