package org.simulationautomation.kubernetesclient.exceptions;

public class PodNotFoundException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 2421882911473543297L;

  public PodNotFoundException(String errorMessage, Throwable err) {
    super(errorMessage, err);
  }

  public PodNotFoundException(String errorMessage) {
    super(errorMessage);
  }

}
