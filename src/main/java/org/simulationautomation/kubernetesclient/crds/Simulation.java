package org.simulationautomation.kubernetesclient.crds;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;

/**
 * POJO class representing a custom ressource for k8s api
 * 
 * @author Niko Benkler
 *
 */
public class Simulation extends CustomResource {

  private static final long serialVersionUID = 7231247064923378701L;
  private SimulationSpec spec;
  private SimulationStatus status;

  public SimulationSpec getSpec() {
    return spec;
  }

  public void setSpec(SimulationSpec spec) {
    this.spec = spec;
  }

  public SimulationStatus getStatus() {
    return status;
  }

  public void setStatus(SimulationStatus status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "Simulation: Name=" + getMetadata().getName() + " apiVersion='" + getApiVersion() + "'"
        + ", metadata=" + getMetadata() + ", spec=" + spec + ", status=" + status + "}";
  }

  @Override
  public ObjectMeta getMetadata() {
    return super.getMetadata();
  }
}
