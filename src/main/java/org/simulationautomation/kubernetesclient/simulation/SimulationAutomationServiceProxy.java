package org.simulationautomation.kubernetesclient.simulation;

import java.util.List;
import org.simulationautomation.kubernetesclient.api.ISimulationAutomationServiceProxy;
import org.simulationautomation.kubernetesclient.api.ISimulationServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class as Proxy between REST-interface and actual backend for operations concerning the
 * simulation automation application.
 * 
 * @author Niko Benkler
 *
 */
@Service
public class SimulationAutomationServiceProxy implements ISimulationAutomationServiceProxy {

  @Autowired
  ISimulationServiceRegistry simulationServiceRegistry;

  @Override
  public List<String> getExistingSimulations() {
    return simulationServiceRegistry.getSimulations();
  }

}
