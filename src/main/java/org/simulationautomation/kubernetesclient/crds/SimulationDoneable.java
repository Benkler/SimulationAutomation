package org.simulationautomation.kubernetesclient.crds;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;


public class SimulationDoneable extends CustomResourceDoneable<Simulation> {
  public SimulationDoneable(Simulation resource, Function function) {
    super(resource, function);
  }
}
