package org.simulationautomation.kubernetesclient.api;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;

/**
 * Implements functionality to watch event thrown by all pod of current cluster
 * 
 * @author Niko Benkler
 *
 */
public interface ISimulationPodWatcher extends Watcher<Pod> {

  @Override
  void eventReceived(Watcher.Action action, Pod pod);

  /**
   * Close kubernetes operator pod
   */
  @Override
  void onClose(KubernetesClientException cause);

}
