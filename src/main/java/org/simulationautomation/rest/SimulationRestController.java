package org.simulationautomation.rest;

import java.net.URI;
import java.net.URISyntaxException;
import org.simulationautomation.kubernetesclient.api.ISimulationServiceProxy;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.exceptions.SimulationCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Component
// TODO base path?
public class SimulationRestController {

  private static final Logger log = LoggerFactory.getLogger(SimulationRestController.class);


  @Autowired
  ISimulationServiceProxy simulationServiceProxy;


  // TODO send experiment data & Post Mapping
  /**
   * Rest-Endpoint to trigger simulation with give simulation data.
   * 
   * @return
   * @throws URISyntaxException
   */
  @GetMapping("/simulation/create")
  public ResponseEntity<String> createSimulation() throws URISyntaxException {

    log.info("Rest Endpoint triggered: Create simulation");


    Simulation simulation;
    try {
      simulation = simulationServiceProxy.createSimulation();
    } catch (SimulationCreationException e) {
      log.info("Bad Request: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Could not create simulation. Error Message: \n" + e.getMessage());
    }
    String simulationName = simulation.getMetadata().getName();
    log.info("Rest Response: Simulation accepted with name = " + simulationName);
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(new URI("/simulation/" + simulationName + "/status"));
    return new ResponseEntity<String>(headers, HttpStatus.ACCEPTED);

  }

  /**
   * Rest Endpoint that provides information about the current status of a simulation specified by
   * its unique name. </br>
   * Error-Response, if Simulation does not exist. </br>
   * OK-Response, if simulation exists but not yet finished. </br>
   * Automatic forwarding via @HttpStatus.SEE_OTHER to rest-endpoint, if simulation succeeded.
   * 
   * @param simulationName
   * @return
   * @throws URISyntaxException
   */
  @GetMapping("/simulation/{simulationName}/status")
  public ResponseEntity<String> getSimulationStatus(
      @PathVariable(name = "simulationName") String simulationName) throws URISyntaxException {

    log.info("Rest Endpoint triggered: Get status of simulation with name=" + simulationName);

    if (!simulationServiceProxy.doesSimulationExist(simulationName)) {
      log.info("Rest Response: Simulation with name=" + simulationName + " does not exist");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Simulation with name=" + simulationName + " does not exist");
    }

    if (simulationServiceProxy.isSimulationFinished(simulationName)) {
      log.info("Rest Response: Simulation with name=" + simulationName + " is finished");
      HttpHeaders headers = new HttpHeaders();
      headers.setLocation(new URI("/simulation/" + simulationName + "/results"));
      return new ResponseEntity<String>(headers, HttpStatus.SEE_OTHER);

    } else {
      // Do nothing
      log.info("Rest Response: Simulation with name=" + simulationName + " is not yet finished");
      return ResponseEntity.ok().build();
    }

  }



  /**
   * Rest end point to get zipped simulation results
   * 
   * @param simulationName
   * @return
   */
  @RequestMapping(value = "/simulation/{simulationName}/results", method = RequestMethod.GET)
  public ResponseEntity<byte[]> getSimulationResults(
      @PathVariable(name = "simulationName") String simulationName) {

    log.info(
        "Rest Endpoint triggered: Get simulation result of simulation with name=" + simulationName);

    // Check if simulation exists
    if (!simulationServiceProxy.doesSimulationExist(simulationName)) {
      String response = "Simulation with name=" + simulationName + " does not exist";
      log.info("Rest Response: " + response);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getBytes());
    }

    // Check if simulation is finished
    if (!simulationServiceProxy.isSimulationFinished(simulationName)) {
      String response = "Simulation with name=" + simulationName + " is not yet finished";
      log.info("Rest Response: " + response);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getBytes());
    }

    // Simulation finished
    byte[] contents = simulationServiceProxy.getSimulationResults(simulationName);

    if (contents == null) {
      String response =
          "Simulation with name=" + simulationName + " encountered an error while loading zip";
      log.info("Rest Response: " + response);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response.getBytes());

    } else {
      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + simulationName);
      headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");

      log.info("Successfully retrieve zip file for simulation with name=" + simulationName);
      return new ResponseEntity<byte[]>(contents, headers, HttpStatus.OK);
    }



  }


  /**
   * Rest end point to get single result file
   * 
   * @param simulationName
   * @return
   */
  @RequestMapping(value = "/simulation/{simulationName}/results/{fileName}/file",
      method = RequestMethod.GET)
  public ResponseEntity<byte[]> getSimulationResultFile(
      @PathVariable(name = "simulationName") String simulationName,
      @PathVariable(name = "fileName") String fileName) {

    log.info("Rest Endpoint triggered: Get file with name=" + fileName
        + " of results for simulation with name=" + simulationName);

    // Check if simulation exists
    if (!simulationServiceProxy.doesSimulationExist(simulationName)) {
      String response = "Simulation with name=" + simulationName + " does not exist";
      log.info("Rest Response: " + response);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getBytes());
    }

    // Check if simulation is finished
    if (!simulationServiceProxy.isSimulationFinished(simulationName)) {
      String response = "Simulation with name=" + simulationName + " is not yet finished";
      log.info("Rest Response: " + response);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getBytes());
    }

    // Simulation finished
    byte[] contents = simulationServiceProxy.getSimulationResultFile(simulationName, fileName);
    if (contents == null) {
      String response = "Simulation with name=" + simulationName
          + " encountered an error while loading file with name=" + fileName;
      log.info("Rest Response: " + response);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response.getBytes());

    } else {

      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + simulationName);
      headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");

      log.info("Successfully retrieved file with name=" + fileName + " for simulation with name="
          + simulationName);
      return new ResponseEntity<byte[]>(contents, headers, HttpStatus.OK);
    }



  }



  @RequestMapping(value = "/simulation/{simulationName}/log", method = RequestMethod.GET)
  public ResponseEntity<byte[]> getSimulationLog(
      @PathVariable(name = "simulationName") String simulationName) {

    log.info(
        "Rest Endpoint triggered: Get simulation result of simulation with name=" + simulationName);

    // Check if simulation exists
    if (!simulationServiceProxy.doesSimulationExist(simulationName)) {
      String response = "Simulation with name=" + simulationName + " does not exist";
      log.info("Rest Response: " + response);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getBytes());
    }

    byte[] simulationLog = simulationServiceProxy.getSimulationLog(simulationName);
    if (simulationLog == null) {
      String response = "Log for Simulation with name=" + simulationName + " does not exist";
      log.info("Rest Response: " + response);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getBytes());
    }



    // Logs successfully retrieved
    log.info("Rest Response: Query logs for Simulation with name=" + simulationName);
    HttpHeaders headers = new HttpHeaders();

    headers.add(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=" + "logs_" + simulationName);
    headers.add(HttpHeaders.CONTENT_TYPE, "text/plain");

    return new ResponseEntity<byte[]>(simulationLog, headers, HttpStatus.OK);



  }



  // TODO Endpunkt für Datei



}
