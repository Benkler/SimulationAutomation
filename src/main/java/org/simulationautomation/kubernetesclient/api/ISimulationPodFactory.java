package org.simulationautomation.kubernetesclient.api;

import org.simulationautomation.kubernetesclient.crds.Simulation;
import io.fabric8.kubernetes.api.model.Pod;

public interface ISimulationPodFactory {

  Pod createSimulationPod(Simulation simulation);

}
