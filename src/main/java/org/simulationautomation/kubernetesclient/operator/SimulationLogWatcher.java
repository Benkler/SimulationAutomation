package org.simulationautomation.kubernetesclient.operator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.simulationautomation.kubernetesclient.api.ISimulationLogWatcher;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.simulation.properties.SimulationPathFactory;
import org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

@Component
public class SimulationLogWatcher implements ISimulationLogWatcher {
  private Logger log = LoggerFactory.getLogger(SimulationLogWatcher.class);

  @Autowired
  KubernetesClient client;

  /**
   * Register LogWatcher for a given pod of a simulation. Logs are written into a file, which is
   * saved within the accompanying simulation folder. Thread is necessary, as we need to wait for
   * the pod to be ready/complete, in order to start watch/close the file output stream.
   * 
   * @param simulation
   * @param simulationPod
   */
  @Override
  public void registerSimulationPodLogWatcher(Simulation simulation, Pod simulationPod) {

    String simulationName = simulation.getMetadata().getName();
    String simulationPodName = simulationPod.getMetadata().getName();
    String pathToLogFile = SimulationPathFactory.getPathToSimulationLogFile(simulationName);

    log.info("Register simulation log watcher for simulation with name=" + simulationName);


    // Create log file
    File logFile = new File(pathToLogFile);
    FileOutputStream fos;
    try {
      logFile.createNewFile();
      fos = new FileOutputStream(logFile);
    } catch (IOException e) {
      log.error("Error while creating log file. ErrorMessage=" + e.getMessage());
      return;
    }


    Thread logThread = new Thread() {

      @Override
      public void run() {

        try {
          // Need to wait for container to be "running"
          client.pods().inNamespace(SimulationProperties.SIMULATION_NAMESPACE)
              .withName(simulationPodName).waitUntilCondition(
                  pod -> pod.getStatus().getPhase().equals("Running"), 30, TimeUnit.SECONDS);

          // Create actual Watcher for logs which writes to FileOutputStream
          client.pods().inNamespace(SimulationProperties.SIMULATION_NAMESPACE)
              .withName(simulationPodName).watchLog(fos);
          log.info("Start simulationg log watch for simulation with name=" + simulationName);

          // Wait until simulation is finished (Timeout after specified value)
          client.pods().inNamespace(SimulationProperties.SIMULATION_NAMESPACE)
              .withName(simulationPodName).waitUntilCondition(
                  pod -> (pod.getStatus().getPhase().equals("Succeeded")
                      || pod.getStatus().getPhase().equals("Failed")),
                  SimulationProperties.SIMULATION_DURATION_MAX_MIN, TimeUnit.MINUTES);

          // Simulation Finished -> Close FileOutputStream
          log.info("Simulation with name=" + simulationName
              + " is finished. Close FileOutputStream of LogWatcher");
          fos.close();
        } catch (InterruptedException e) {
          log.error("Logging error while waiting on simulation with name=" + simulationName, e);
        } catch (IOException e) {
          log.error("Cannot close FileOutputStream", e);;
        }



      }
    };

    logThread.start();



  }

}
