package org.simulationautomation.kubernetesclient.api;

import org.simulationautomation.kubernetesclient.crds.Simulation;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;


public interface ISimulationWatcher extends Watcher<Simulation> {


  @Override
  void eventReceived(Watcher.Action action, Simulation simulation);

  @Override
  void onClose(KubernetesClientException cause);

}
