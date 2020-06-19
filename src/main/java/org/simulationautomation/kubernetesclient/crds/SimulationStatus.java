package org.simulationautomation.kubernetesclient.crds;

import org.simulationautomation.kubernetesclient.simulation.SimulationStatusCode;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class SimulationStatus implements KubernetesResource {


  private static final long serialVersionUID = 7606654973979034913L;


  SimulationStatusCode statusCode;



  public SimulationStatusCode getStatusCode() {
    return statusCode;
  }



  public void setStatus(SimulationStatusCode status) {
    this.statusCode = status;
  }



  @Override
  public String toString() {
    return "SimulationStatus{ Status=" + statusCode + "}";
  };
}
