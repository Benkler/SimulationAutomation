package org.simulationautomation.kubernetesclient.util;

import java.util.Collections;
import org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionNames;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionSpec;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionVersion;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

/**
 * Build Simulation CRD on startup and provided CRD context.
 * 
 * @author Niko Benkler
 *
 */
@Component(value = "simulationCRDBuilder")
public class SimulationCRDUtil {

  private static final Logger log = LoggerFactory.getLogger(SimulationCRDUtil.class);

  private static final String VERSION = "v1";

  /**
   * Get custom resource definition for kind=simulation
   */
  public static CustomResourceDefinition getCRD() {

    ObjectMeta metadata = new ObjectMeta();
    metadata.setName(SimulationProperties.SIMULATION_CRD_NAME);

    CustomResourceDefinitionVersion crdVersion = new CustomResourceDefinitionVersion();
    crdVersion.setName(VERSION);
    crdVersion.setServed(true);
    crdVersion.setStorage(true);

    CustomResourceDefinitionNames crdNames = new CustomResourceDefinitionNames();
    crdNames.setPlural(SimulationProperties.SIMULATION_NAME_PLURAL);
    crdNames.setSingular(SimulationProperties.SIMULATION_NAME);
    crdNames.setKind(SimulationProperties.SIMULATION_KIND);
    crdNames.setShortNames(Collections.singletonList(SimulationProperties.SIMULATION_SHORT_NAME));


    CustomResourceDefinitionSpec crdSpec = new CustomResourceDefinitionSpec();
    crdSpec.setGroup(SimulationProperties.SIMULATION_GROUP);
    crdSpec.setVersions(Collections.singletonList(crdVersion));
    crdSpec.setScope(SimulationProperties.SIMULATION_SCOPE);
    crdSpec.setNames(crdNames);

    CustomResourceDefinition simulationCRD = new CustomResourceDefinition();
    simulationCRD.setApiVersion("apiextensions.k8s.io/v1beta1");
    simulationCRD.setMetadata(metadata);
    simulationCRD.setSpec(crdSpec);

    log.info("Custom resource definition successfully created");

    return simulationCRD;
  }

  public CustomResourceDefinitionContext getCRDContext() {
    return new CustomResourceDefinitionContext.Builder()
        .withGroup(SimulationProperties.SIMULATION_GROUP)
        .withScope(SimulationProperties.SIMULATION_SCOPE).withVersion(VERSION)
        .withPlural(SimulationProperties.SIMULATION_NAME_PLURAL).build();

  }

}
