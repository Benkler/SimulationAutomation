package org.simulationautomation.rest;

import java.util.List;
import org.simulationautomation.kubernetesclient.api.ISimulationAutomationServiceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.gson.Gson;

@RestController
@Component
public class SimulationAutomationStatusController {

  private static final Logger log =
      LoggerFactory.getLogger(SimulationAutomationStatusController.class);
  @Autowired
  ISimulationAutomationServiceProxy simulationAutomationServiceProxy;

  @GetMapping("/simulationautomation/simulations")
  public ResponseEntity<String> getSimulationStatus() {
    log.info("Rest Endpoint triggered: Query all active simulations");
    List<SimulationVO> existingSimulations =
        simulationAutomationServiceProxy.getExistingSimulations();
    Gson gson = new Gson();
    String responseBody = gson.toJson(existingSimulations);

    log.info("Following simulations exist: " + existingSimulations.toString());
    return new ResponseEntity<String>(responseBody, HttpStatus.OK);

  }

}
