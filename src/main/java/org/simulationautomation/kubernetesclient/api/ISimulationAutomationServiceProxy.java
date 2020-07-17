package org.simulationautomation.kubernetesclient.api;

import java.util.List;
import org.simulationautomation.kubernetesclient.rest.SimulationVO;

public interface ISimulationAutomationServiceProxy {

  List<String> getNamesOfExistingSimulations();

  List<SimulationVO> getExistingSimulations();

}
