package org.simulationautomation.rest;

import java.net.URI;
import java.net.URISyntaxException;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.exceptions.SimulationCreationException;
import org.simulationautomation.kubernetesclient.operator.SimulationOperator;
import org.simulationautomation.kubernetesclient.simulation.SimulationServiceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Component
public class SimulationRestController {

  private static final Logger log = LoggerFactory.getLogger(SimulationRestController.class);

  @Autowired
  SimulationOperator operator;

  @Autowired
  SimulationServiceProxy simulationServiceProxy;


  // TODO send experiment data
  @GetMapping("/simulation/create")
  public ResponseEntity<String> createSimulation() throws URISyntaxException {

    log.info("Rest Endpoint triggered: Create simulation");



    Simulation simulation;
    try {
      simulation = operator.createSimulation();
    } catch (SimulationCreationException e) {
      log.info("Bad Request: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Could not create simulation. Error Message: \n" + e.getMessage());
    }

    log.info(
        "Rest Response: Simulation accepted with name = " + simulation.getMetadata().getName());
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(
        new URI("/simulation/status?simulationName=" + simulation.getMetadata().getName()));
    return new ResponseEntity<String>(headers, HttpStatus.ACCEPTED);

  }

  @GetMapping("/simulation/status")
  public ResponseEntity<String> getSimulationStatus(
      @RequestParam(name = "simulationName") String simulationName) throws URISyntaxException {

    log.info("Rest Endpoint triggered: Get status of simulation with name=" + simulationName);

    if (!simulationServiceProxy.doesSimulationExist(simulationName)) {
      log.info("Rest Response: Simulation with name=" + simulationName + " does not exist");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Simulation with name=" + simulationName + " does not exist");
    }

    if (simulationServiceProxy.isSimulationFinished(simulationName)) {
      log.info("Rest Response: Simulation with name=" + simulationName + " is finished");
      HttpHeaders headers = new HttpHeaders();
      headers.setLocation(new URI("/simulation/results?simulationName=" + simulationName));
      return new ResponseEntity<String>(headers, HttpStatus.SEE_OTHER);

    } else {
      // Do nothing
      log.info("Rest Response: Simulation with name=" + simulationName + " is not yet finished");
      return ResponseEntity.ok().build();
    }

  }

  @GetMapping("/simulation/results")
  public ResponseEntity<String> getSimulationResults(
      @RequestParam(name = "simulationName") String simulationName) {

    log.info(
        "Rest Endpoint triggered: Get simulation result of simulation with name=" + simulationName);
    if (!simulationServiceProxy.doesSimulationExist(simulationName)) {
      log.info("Rest Response: Simulation with name=" + simulationName + " does not exist");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Simulation with name=" + simulationName + " does not exist");
    }

    if (!simulationServiceProxy.isSimulationFinished(simulationName)) {
      log.info("Rest Response: Simulation with name=" + simulationName + " is not yet finished");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Simulation with name=" + simulationName + " is not yet finished");
    }

    log.info("Rest Response: Query results for Simulation with name=" + simulationName);
    HttpHeaders headers = new HttpHeaders();
    // TODO set result
    return new ResponseEntity<String>(headers, HttpStatus.OK);
  }

}
