package org.simulationautomation.kubernetesclient.operator;

import static org.simulationautomation.kubernetesclient.simulation.SimulationCRDs.SIMULATION_CRD_GROUP;
import static org.simulationautomation.kubernetesclient.simulation.SimulationCRDs.SIMULATION_CRD_KIND;
import static org.simulationautomation.kubernetesclient.simulation.SimulationCRDs.SIMULATION_CRD_NAME;
import static org.simulationautomation.kubernetesclient.simulation.SimulationCRDs.SIMULATION_NAMESPACE;

import java.util.List;

import org.simulationautomation.kubernetesclient.api.ICustomResourceDefinitionBuilder;
import org.simulationautomation.kubernetesclient.crds.SimulationCR;
import org.simulationautomation.kubernetesclient.crds.SimulationCRDoneable;
import org.simulationautomation.kubernetesclient.crds.SimulationCRList;
import org.simulationautomation.kubernetesclient.simulation.SimulationService;
import org.simulationautomation.kubernetesclient.util.CustomNamespaceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

@Component
public class SimulationOperator {

	private Logger log = LoggerFactory.getLogger(SimulationOperator.class);

	private CustomResourceDefinition simulationCRD = null;

	private String simulationResourceVersion;

	private NonNamespaceOperation<SimulationCR, SimulationCRList, SimulationCRDoneable, Resource<SimulationCR, SimulationCRDoneable>> simulationCRDClient;

	@Autowired
	private SimulationService simulationsService;

	@Autowired
	private SimulationWatcher simulationWatcher;

	@Autowired
	private K8SCoreRuntime k8SCoreRuntime;

	@Autowired
	CustomNamespaceBuilder nsBuilder;

	@Autowired
	@Qualifier("simulationCRDBuilder")
	ICustomResourceDefinitionBuilder crdBuilder;

	/*
	 * Init can only be called if all the required CRDs are present - It creates the
	 * CRD clients to be able to watch and execute operations - It loads the
	 * existing resources (current state in the cluster) - It register the watches
	 * for our CRDs
	 */
	public void init() {
		nsBuilder.createNamespace(SIMULATION_NAMESPACE);
		registerSimulationCRD();
		// Creating CRDs Clients
		simulationCRDClient = k8SCoreRuntime.customResourcesClient(simulationCRD, SimulationCR.class,
				SimulationCRList.class, SimulationCRDoneable.class);
		loadExistingResources();
		registerSimulationWatch();

	}

	/**
	 * List custom Resources for type Simulation in given namepsace
	 * 
	 * @param namespace
	 * @return
	 * @return
	 */
	public List<SimulationCR> listExistingSimulations() {
		return simulationCRDClient.list().getItems();
	}

	private void registerSimulationCRD() {

		simulationCRD = crdBuilder.getCRD();
		k8SCoreRuntime.registerCustomResourceDefinition(simulationCRD);
		k8SCoreRuntime.registerCustomKind(SIMULATION_CRD_GROUP + "/v1", SIMULATION_CRD_KIND, SimulationCR.class);

		log.info("Custom resource definition successfully registered");
	}

	/*
	 * Load existing instances of our CRDs - This checks the existing resources and
	 * make sure that they are loaded correctly - This also performs the binding of
	 * a service to its app
	 */
	private void loadExistingResources() {
		// Load Existing Applications
		List<SimulationCR> applicationList = simulationCRDClient.list().getItems();
		if (!applicationList.isEmpty()) {
			simulationResourceVersion = applicationList.get(0).getMetadata().getResourceVersion();
			log.info("Simulation Resource Version: " + simulationResourceVersion);
			applicationList.forEach(app -> {
				simulationsService.addSimulation(app.getMetadata().getName(), app);
				log.info("Simulation " + app.getMetadata().getName() + " found. Add to SimulationService.");
			});

		}

	}

	/*
	 * Register Simulation Watch - This watch is in charge of adding and removing
	 * apps to/from the In memory desired state
	 */
	private void registerSimulationWatch() {
		log.info("> Registering Application CRD Watch");
		simulationCRDClient.withResourceVersion(simulationResourceVersion).watch(simulationWatcher);

	}

	/*
	 * Check that all the CRDs are found for this operator to work
	 */
	private boolean allCRDsFound() {
		if (simulationCRD == null) {
			return false;
		}
		return true;
	}

	/*
	 * Check for Required CRDs
	 */
	private boolean areRequiredCRDsPresent() {
		try {

			k8SCoreRuntime.registerCustomKind(SIMULATION_CRD_GROUP + "/v1", SIMULATION_CRD_KIND, SimulationCR.class);

			CustomResourceDefinitionList crds = k8SCoreRuntime.getCustomResourceDefinitionList();
			for (CustomResourceDefinition crd : crds.getItems()) {
				ObjectMeta metadata = crd.getMetadata();
				if (metadata != null) {
					String name = metadata.getName();

					if (SIMULATION_CRD_NAME.equals(name)) {
						simulationCRD = crd;
					}
				}
			}
			if (allCRDsFound()) {
				return true;
			} else {
				log.error("Custom CRDs not available");

				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("> Init sequence not done");
		}
		return false;
	}

}
