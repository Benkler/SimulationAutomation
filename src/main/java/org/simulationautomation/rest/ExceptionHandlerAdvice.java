package org.simulationautomation.rest;

import org.simulationautomation.kubernetesclient.exceptions.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Exception Handler for SimulationCreationException
 * 
 * @author Niko Benkler
 *
 */
@ControllerAdvice
public class ExceptionHandlerAdvice {

  private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

  public ResponseEntity<?> handleException(RestClientException e) {

    log.error(
        "Kubernetes Rest Client Exception Exception caught with error message:" + e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

  }

}
