package org.simulationautomation.kubernetesclient.crds;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.fabric8.kubernetes.api.model.KubernetesResource;

/**
 * Specification POJO for k8s API
 * 
 * @author Niko Benkler
 *
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class SimulationCRSpec implements KubernetesResource {

	private static final long serialVersionUID = -424650618584495148L;

	private int replicas;
	private String image;

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public int getReplicas() {
		return replicas;
	}

	@Override
	// TODO adapt
	public String toString() {
		return "SimulationCRSpec{replicas=" + replicas + "}";
	}

	public void setReplicas(int replicas) {
		this.replicas = replicas;
	}

}
