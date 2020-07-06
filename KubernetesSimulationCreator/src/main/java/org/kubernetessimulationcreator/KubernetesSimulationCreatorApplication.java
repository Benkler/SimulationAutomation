package org.kubernetessimulationcreator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KubernetesSimulationCreatorApplication {

  public static void main(String[] args) {
    SpringApplication.run(KubernetesSimulationCreatorApplication.class, args);
    ModelLoader ml = new ModelLoader();
    ml.test();
  }

}
