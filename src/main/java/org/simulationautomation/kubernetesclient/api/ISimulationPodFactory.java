package org.simulationautomation.kubernetesclient.api;

import org.simulationautomation.kubernetesclient.crds.Simulation;
import io.fabric8.kubernetes.api.model.Pod;

/**
 * Factory class that creates a Kubernetes Pod to execute a Simulation. This is: Define docker image
 * with the palladio instance to execute the simulation and the mount paths for this image,
 * containing the experiment data.
 * 
 * @author Niko Benkler
 *
 */
public interface ISimulationPodFactory {

  /**
   * Create Kubernetes Pod with all necessary settings to execute a simulation.
   * 
   * @param simulation
   * @return
   */
  Pod createSimulationPod(Simulation simulation);

}
