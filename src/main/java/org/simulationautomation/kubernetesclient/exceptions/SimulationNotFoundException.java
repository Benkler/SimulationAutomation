package org.simulationautomation.kubernetesclient.exceptions;

public class SimulationNotFoundException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 683341068117754858L;

  public SimulationNotFoundException(String errorMessage, Throwable err) {
    super(errorMessage, err);
  }

  public SimulationNotFoundException(String errorMessage) {
    super(errorMessage);
  }

}
