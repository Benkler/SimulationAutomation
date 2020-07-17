package org.simulationautomation.kubernetesclient.util;

import static org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties.SIMULATION_GROUP;
import static org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties.SIMULATION_KIND;
import static org.simulationautomation.kubernetesclient.simulation.properties.SimulationProperties.SIMULATION_NAMESPACE;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.simulationautomation.kubernetesclient.api.IK8SCoreRuntime;
import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.simulationautomation.kubernetesclient.crds.SimulationDoneable;
import org.simulationautomation.kubernetesclient.crds.SimulationList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

/**
 * Utility class to create Kubernetes Client on Startup which can be autowired by any other classes.
 * 
 * @author Niko Benkler
 *
 */
@Configuration
public class ClientProducer {

  private static final Logger log = LoggerFactory.getLogger(ClientProducer.class);

  @Autowired
  private IK8SCoreRuntime k8SCoreRuntime;

  /**
   * Find current namespace (magic file in specified path)
   * 
   * @return
   */
  @Bean(name = "namespace")
  @Scope("singleton")
  public String findMyCurrentNameSpace() {

    try {
      String nameSpace = new String(
          Files.readAllBytes(Paths.get("var/run/secrets/kubernetes.io/serviceaccount/namespace")));
      log.info("Current namespace=" + nameSpace);
      return nameSpace;
    } catch (IOException e) {
      log.warn(
          "Error while retrieving namespace. Message: " + e + "\n Fallback to namespace 'default'");
      return "default";
    }

  }

  /**
   * Make Default Kubernetes Client for basic calls to kubernetes api
   * 
   * @param namespace
   * @return
   */
  @Bean
  @Scope("singleton")
  public KubernetesClient makeDefaultClient(@Qualifier("namespace") String namespace) {

    log.info("Kubernetes Client created in namespace=" + namespace);

    return new DefaultKubernetesClient().inNamespace(namespace);

  }

  /**
   * Make Simulation Client for specific simulation-related calls to kubernetes api
   * 
   * @return
   */
  @Bean
  @Scope("singleton")
  public NonNamespaceOperation<Simulation, SimulationList, SimulationDoneable, Resource<Simulation, SimulationDoneable>> makeSimulationClient() {
    CustomResourceDefinition simulationCRD = createAndRegisterSimulationCRD();
    // Creating CRD Client for simulation
    return k8SCoreRuntime.customResourcesClientInNameSpace(simulationCRD, Simulation.class,
        SimulationList.class, SimulationDoneable.class, SIMULATION_NAMESPACE);
  }



  private CustomResourceDefinition createAndRegisterSimulationCRD() {

    CustomResourceDefinition simuCRD = SimulationCRDUtil.getCRD();
    k8SCoreRuntime.registerCustomResourceDefinition(simuCRD);
    k8SCoreRuntime.registerCustomKind(SIMULATION_GROUP + "/v1", SIMULATION_KIND, Simulation.class);

    log.info("SimulationCRD successfully registered");
    return simuCRD;
  }

}
