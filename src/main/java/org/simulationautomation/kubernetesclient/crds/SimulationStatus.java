package org.simulationautomation.kubernetesclient.crds;

import org.simulationautomation.kubernetesclient.simulation.SimulationStatusCode;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class SimulationStatus implements KubernetesResource {


  private static final long serialVersionUID = 7606654973979034913L;


  SimulationStatusCode status;



  public SimulationStatusCode getStatus() {
    return status;
  }



  public void setStatus(SimulationStatusCode status) {
    this.status = status;
  }



  @Override
  public String toString() {
    return "SimulationStatus{ Status=" + status + "}";
  };
}
