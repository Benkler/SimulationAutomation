package org.simulationautomation.kubernetesclient.exceptions;

public class SimulationCreationException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 2421882911473543297L;

  public SimulationCreationException(String errorMessage, Throwable err) {
    super(errorMessage, err);
  }

  public SimulationCreationException(String errorMessage) {
    super(errorMessage);
  }

}
