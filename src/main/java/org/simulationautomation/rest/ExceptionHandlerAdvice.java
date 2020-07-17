package org.simulationautomation.rest;

import org.simulationautomation.kubernetesclient.exceptions.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Exception Handler for SimulationCreationException
 * 
 * @author Niko Benkler
 *
 */
@ControllerAdvice
public class ExceptionHandlerAdvice extends ResponseEntityExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

  @ExceptionHandler(RestClientException.class)
  public ResponseEntity<Object> handleException(RestClientException e, WebRequest request) {

    log.error(
        "Kubernetes Rest Client Exception Exception caught with error message:" + e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

  }

}
