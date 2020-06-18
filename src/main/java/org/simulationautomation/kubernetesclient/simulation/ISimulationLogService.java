package org.simulationautomation.kubernetesclient.simulation;

import org.simulationautomation.kubernetesclient.exceptions.PodNotFoundException;
import io.fabric8.kubernetes.client.dsl.LogWatch;


public interface ISimulationLogService {

  /**
   * Get logs for pod with specified name.
   * 
   * @param podName
   * @param namespace
   * @return
   * @throws PodNotFoundException
   */
  String getLogs(String simulationName) throws PodNotFoundException;

  LogWatch getLogWatcher(String simulationName);

}
