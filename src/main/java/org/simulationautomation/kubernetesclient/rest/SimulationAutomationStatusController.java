package org.simulationautomation.kubernetesclient.rest;

import java.util.List;
import org.simulationautomation.kubernetesclient.api.ISimulationAutomationServiceProxy;
import org.simulationautomation.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest-Controller for any other REST-queries to the client
 * 
 * @author Niko Benkler
 *
 */
@RestController
@Component
public class SimulationAutomationStatusController {

  private static final Logger log =
      LoggerFactory.getLogger(SimulationAutomationStatusController.class);
  @Autowired
  ISimulationAutomationServiceProxy simulationAutomationServiceProxy;

  /**
   * Rest-Endpoint to retrieve all existing simulations, regardless whether they are still running
   * or already finished. </br>
   * 
   * @return List of SimulationVO in JSON-Format
   */
  @GetMapping("/simulationautomation/simulations")
  public ResponseEntity<String> getSimulationStatus() {
    log.info("Rest Endpoint triggered: Query all active simulations");
    List<SimulationVO> existingSimulations =
        simulationAutomationServiceProxy.getExistingSimulations();


    String responseBody = JSONUtil.getInstance().toJson(existingSimulations);

    log.info("Following simulations exist: " + existingSimulations.toString());
    return new ResponseEntity<String>(responseBody, HttpStatus.OK);

  }

  /**
   * Simple Rest-Response to check whether the client is up and running. As this SpringApplication
   * runs also in a kubernetes pod, we don't need to check any other conditions.
   * 
   * @return
   */
  @GetMapping("/simulationautomation/client")
  public ResponseEntity<String> isClientActive() {
    log.info("Rest Endpoint triggered: isClientActive");

    return new ResponseEntity<String>(HttpStatus.OK);

  }

}
