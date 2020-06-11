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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
  @RequestMapping("/simulation/create")
  public ResponseEntity<String> createSimulation() throws URISyntaxException {

    log.info("Rest Endpoint triggered: Create simulation");



    Simulation simulation;
    try {
      simulation = operator.createSimulation();
    } catch (SimulationCreationException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Could not create simulation. Error Message: \n" + e.getMessage());
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(new URI("/simulation/status/" + simulation.getMetadata().getName()));
    return new ResponseEntity<String>(headers, HttpStatus.ACCEPTED);

  }

  @RequestMapping("/simulation/status/{simulationName}")
  public ResponseEntity<String> getSimulationStatus(@PathVariable String simulationName)
      throws URISyntaxException {


    if (!simulationServiceProxy.doesSimulationExist(simulationName)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Simulation with name=" + simulationName + " does not exist");
    }

    if (simulationServiceProxy.isSimulationFinished(simulationName)) {
      HttpHeaders headers = new HttpHeaders();
      headers.setLocation(new URI("/simulation/results/" + simulationName));
      return new ResponseEntity<String>(headers, HttpStatus.SEE_OTHER);

    } else {
      // Do nothing
      return ResponseEntity.ok().build();
    }

  }

  @RequestMapping("/simulation/results/{simulationName}")
  public ResponseEntity<String> getSimulationResults(@PathVariable String simulationName) {
    if (!simulationServiceProxy.doesSimulationExist(simulationName)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Simulation with name=" + simulationName + " does not exist");
    }

    if (!simulationServiceProxy.isSimulationFinished(simulationName)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Simulation with name=" + simulationName + " os not yet finished");
    }


    HttpHeaders headers = new HttpHeaders();
    // TODO set result
    return new ResponseEntity<String>(headers, HttpStatus.OK);
  }

}
