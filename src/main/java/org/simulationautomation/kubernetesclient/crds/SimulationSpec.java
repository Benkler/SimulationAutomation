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
public class SimulationSpec implements KubernetesResource {

  private static final long serialVersionUID = -424650618584495148L;


  String uuid;



  public String getUuid() {
    return uuid;
  }



  public void setUuid(String uuid) {
    this.uuid = uuid;
  }



  @Override
  // TODO adapt
  public String toString() {
    return "SimulationSpec{uuid=" + uuid + "}";
  }


}
