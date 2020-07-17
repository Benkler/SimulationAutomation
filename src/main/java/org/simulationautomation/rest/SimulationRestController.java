package org.simulationautomation.rest;

import java.net.URI;
import java.net.URISyntaxException;
import org.simulationautomation.kubernetesclient.api.ISimulationServiceProxy;
import org.simulationautomation.kubernetesclient.exceptions.RestClientException;
import org.simulationautomation.kubernetesclient.simulation.SimulationStatusCode;
import org.simulationautomation.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Rest Controller to handle REST-Requests for single simulations.
 * 
 * @author Niko Benkler
 *
 */
@RestController
@Component
public class SimulationRestController {

  private static final Logger log = LoggerFactory.getLogger(SimulationRestController.class);


  @Autowired
  ISimulationServiceProxy simulationServiceProxy;


  /**
   * Rest-Endpoint to trigger simulation with given simulation data.
   * 
   * @return
   * @throws URISyntaxException
   * @throws RestClientException
   */
  @PostMapping("/simulation/create")
  public ResponseEntity<?> createSimulation(@RequestParam("file") MultipartFile file)
      throws URISyntaxException, RestClientException {

    log.info("Rest Endpoint triggered: Create simulation");

    SimulationVO simulation = simulationServiceProxy.createSimulation(file);
    String simulationName = simulation.getSimulationName();
    String content = JSONUtil.getInstance().toJson(simulation);

    log.info("Rest Response: Simulation accepted with name = " + simulationName);
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(new URI("/simulation/" + simulationName + "/status"));

    return new ResponseEntity<>(content, headers, HttpStatus.ACCEPTED);

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
   * @throws RestClientException
   */
  @GetMapping("/simulation/{simulationName}/status")
  public ResponseEntity<?> getSimulationStatus(
      @PathVariable(name = "simulationName") String simulationName)
      throws URISyntaxException, RestClientException {

    log.info("Rest Endpoint triggered: Get status of simulation with name=" + simulationName);

    SimulationStatusCode statusCode = simulationServiceProxy.getSimulationStatus(simulationName);

    if (statusCode == SimulationStatusCode.SUCCEEDED) {
      log.info("Rest Response: Simulation with name=" + simulationName + " is finished");
      HttpHeaders headers = new HttpHeaders();
      headers.setLocation(new URI("/simulation/" + simulationName + "/results"));
      return new ResponseEntity<>(statusCode.toString(), headers, HttpStatus.SEE_OTHER);

    } else {
      // Do nothing, as simulation is still running
      log.info("Rest Response: Simulation with name=" + simulationName + " is not yet finished");
      return new ResponseEntity<>(statusCode.toString(), HttpStatus.OK);
    }

  }



  /**
   * Rest end point to get zipped simulation results for specified simulation
   * 
   * @param simulationName
   * @return
   * @throws RestClientException
   */
  @RequestMapping(value = "/simulation/{simulationName}/results", method = RequestMethod.GET)
  public ResponseEntity<byte[]> getSimulationResults(
      @PathVariable(name = "simulationName") String simulationName) throws RestClientException {

    log.info(
        "Rest Endpoint triggered: Get simulation result of simulation with name=" + simulationName);


    // Includes check if simulation exists and is finished
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
   * @throws RestClientException
   */
  @RequestMapping(value = "/simulation/{simulationName}/results/{fileName}",
      method = RequestMethod.GET)
  public ResponseEntity<?> getSimulationResultFile(
      @PathVariable(name = "simulationName") String simulationName,
      @PathVariable(name = "fileName") String fileName) throws RestClientException {

    log.info("Rest Endpoint triggered: Get file with name=" + fileName
        + " of results for simulation with name=" + simulationName);

    // Simulation finished
    byte[] contents = simulationServiceProxy.getSimulationResultFile(simulationName, fileName);
    log.info("Successfully retrieved file with name=" + fileName + " for simulation with name="
        + simulationName);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
    headers.add(HttpHeaders.CONTENT_TYPE, "text/plain");

    return new ResponseEntity<>(contents, headers, HttpStatus.OK);



  }



  @RequestMapping(value = "/simulation/{simulationName}/log", method = RequestMethod.GET)
  public ResponseEntity<?> getSimulationLog(
      @PathVariable(name = "simulationName") String simulationName) throws RestClientException {

    log.info(
        "Rest Endpoint triggered: Get simulation result of simulation with name=" + simulationName);

    byte[] simulationLog = simulationServiceProxy.getSimulationLog(simulationName);

    // Logs successfully retrieved
    log.info("Rest Response: Query logs for Simulation with name=" + simulationName);
    HttpHeaders headers = new HttpHeaders();

    headers.add(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=" + "logs_" + simulationName);
    headers.add(HttpHeaders.CONTENT_TYPE, "text/plain");

    return new ResponseEntity<>(simulationLog, headers, HttpStatus.OK);



  }



}
