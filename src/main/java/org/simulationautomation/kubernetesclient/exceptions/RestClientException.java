package org.simulationautomation.kubernetesclient.exceptions;

public class RestClientException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 2981653213178518152L;


  public RestClientException(String errorMessage) {
    super(errorMessage);
  }

  public RestClientException(String errorMessage, Throwable err) {
    super(errorMessage, err);
  }


}
