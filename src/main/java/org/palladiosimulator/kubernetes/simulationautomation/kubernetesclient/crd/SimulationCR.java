package org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.crd;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;

/**
 * POJO class representing a custom ressource for k8s api
 * 
 * @author Niko Benkler
 *
 */
public class SimulationCR extends CustomResource {

	private static final long serialVersionUID = 7231247064923378701L;
	private SimulationCRSpec spec;
	private SimulationCRStatus status;

	public SimulationCRSpec getSpec() {
		return spec;
	}

	public void setSpec(SimulationCRSpec spec) {
		this.spec = spec;
	}

	public SimulationCRStatus getStatus() {
		return status;
	}

	public void setStatus(SimulationCRStatus status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Simulation" + "apiVersion='" + getApiVersion() + "'" + ", metadata=" + getMetadata() + ", spec=" + spec
				+ ", status=" + status + "}";
	}

	@Override
	public ObjectMeta getMetadata() {
		return super.getMetadata();
	}
}
