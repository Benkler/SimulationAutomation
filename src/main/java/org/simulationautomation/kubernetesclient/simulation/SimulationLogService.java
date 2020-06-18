package org.simulationautomation.kubernetesclient.simulation;

import org.simulationautomation.kubernetesclient.api.ISimulationOperator;
import org.simulationautomation.kubernetesclient.exceptions.PodNotFoundException;
import org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.LogWatch;

@Service
public class SimulationLogService implements ISimulationLogService {

  @Autowired
  KubernetesClient client;

  @Autowired
  ISimulationOperator simulationOperator;

  /**
   * Get logs for pod with specified name.
   * 
   * @param podName
   * @param namespace
   * @return
   */

  @Override
  public String getLogs(String simulationName) throws PodNotFoundException {

    Pod simulationPod = simulationOperator.getPodBySimulationName(simulationName);
    if (simulationPod == null) {

      throw new PodNotFoundException(
          "Could not find Pod for simulation with name=" + simulationName);

    } else {
      return client.pods().inNamespace(SimulationProperties.SIMULATION_NAMESPACE)
          .withName(simulationPod.getMetadata().getName()).getLog();
    }

  }

  @Override
  public LogWatch getLogWatcher(String simulationName) {
    // TODO Auto-generated method stub
    return null;
  }

}
