package org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient;

import java.io.IOException;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

@Component
public class CustomResourceBuilder {

	@Autowired
	KubernetesClient client;

	@Autowired
	CustomResourceDefinitionBuilder crdBuilder;
	private static final Logger log = LoggerFactory.getLogger(CustomResourceBuilder.class);

	public void createCustomResource(String name, String namespace) {
		log.info("Trying to create resource with name=" + name + " in namespace=" + namespace);

		CustomResourceDefinitionContext crdContext = crdBuilder.getCRDContext();

		try {
			// Creating from HashMap
			Map<String, Object> crTemplate = client.customResource(crdContext)
					.load(CustomResourceBuilder.class.getResourceAsStream("/simulation_cr_template.yml"));

			log.info(crTemplate.toString());
			// Need Gson to convert from Map properly
			Gson cr = new Gson();
			cr.toJson(crTemplate);
			// Creating from JSON String
			JSONObject customResource = new JSONObject(cr.toJson(crTemplate));

			// Overwrite metadata
			customResource.getJSONObject("metadata").put("name", name);

			client.customResource(crdContext).create(namespace, customResource.toString());
			log.info("Resource successfully created");
		} catch (IOException | JSONException e) {
			log.error("Error while creating resource. Message= " + e.getMessage());
		}

	}

}
