package org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.impl;

import java.io.IOException;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.api.ICustomResourceBuilder;
import org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.api.ICustomResourceDefinitionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

/**
 * Builder for Simulation CRD's
 * 
 * @author Niko Benkler
 *
 */
@Component(value = "simulationCRBuilder")
public class SimulationCustomResourceBuilder implements ICustomResourceBuilder {

	@Autowired
	KubernetesClient client;

	@Autowired
	@Qualifier("simulationCRDBuilder")
	ICustomResourceDefinitionBuilder crdBuilder;

	private static final Logger log = LoggerFactory.getLogger(SimulationCustomResourceBuilder.class);

	private static final String PATH_TO_TEMPLATE = "/simulation_cr_template.yml";

	/**
	 * Create custom resource according to crDefinition in
	 * "simulation_cr_template.yml" with specified name in namespace
	 * 
	 * @param name
	 * @param namespace
	 * @return
	 */
	@Override
	public Map<String, Object> createCustomResource(String name, String namespace) {
		log.info("Trying to create resource with name=" + name + " in namespace=" + namespace);

		CustomResourceDefinitionContext crdContext = crdBuilder.getCRDContext();
		Map<String, Object> resource = null;

		try {
			// Creating from HashMap
			Map<String, Object> crTemplate = client.customResource(crdContext)
					.load(SimulationCustomResourceBuilder.class.getResourceAsStream(PATH_TO_TEMPLATE));

			// Need Gson to convert from Map properly
			Gson cr = new Gson();
			cr.toJson(crTemplate);
			// Creating from JSON String
			JSONObject customResource = new JSONObject(cr.toJson(crTemplate));

			// Overwrite metadata
			customResource.getJSONObject("metadata").put("name", name);
			customResource.getJSONObject("metadata").put("namespace", namespace);

			// Create Resource in namespace
			resource = client.customResource(crdContext).create(namespace, customResource.toString());
			log.info("Resource successfully created");
		} catch (IOException | JSONException e) {
			log.error("Error while creating resource. Message= " + e.getMessage());
		}

		return resource;

	}

}
