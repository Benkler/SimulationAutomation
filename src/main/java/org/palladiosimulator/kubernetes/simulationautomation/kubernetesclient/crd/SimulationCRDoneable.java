package org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.crd;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;


public class SimulationCRDoneable extends CustomResourceDoneable<SimulationCR> {
    public SimulationCRDoneable(SimulationCR resource, Function function) { super(resource, function); }
}
