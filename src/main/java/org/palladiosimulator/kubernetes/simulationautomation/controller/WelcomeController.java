package org.palladiosimulator.kubernetes.simulationautomation.controller;

import java.util.List;

import org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.api.ICustomResourceBuilder;
import org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.api.ICustomResourceDefinitionBuilder;
import org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.old.SimulationCustomClientFactory;
import org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.util.CustomNamespaceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.P;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

@RestController
@Component
public class WelcomeController {

	private static final Logger log = LoggerFactory.getLogger(WelcomeController.class);

	@Autowired
	KubernetesClient client;

	@Autowired
	ICustomResourceDefinitionBuilder crdBuilder;

	@Autowired
	ICustomResourceBuilder crBuilder;

	@Autowired
	CustomNamespaceBuilder nsBuilder;

	@RequestMapping("/greeting")
	public String greeting() {
		log.info("RestEndpoint triggered");

		Div div = new Div();

		List<Pod> podList = client.pods().list().getItems();

		P p = new P().appendText(
				"Welcome! There are " + podList.size() + " pods registered in namespace:" + client.getNamespace());
		div.appendChild(p);

		for (Pod pod : podList) {
			StringBuilder sb = new StringBuilder();
			String podName = pod.getMetadata().getName();
			String podUid = pod.getMetadata().getUid();
			sb.append("pod name=" + podName + "\n");
			sb.append("		pod id=" + podUid + "\n");

			P podP = new P();
			podP.appendText(sb.toString());
			div.appendChild(podP);
		}

		return div.write();
	}

	@RequestMapping("/create")
	public String createRessource() {

		nsBuilder.createNamespace("otherspace");

		crBuilder.createCustomResource("test8", "otherspace");

		return "Try to create ressource";
	}

	@Autowired
	SimulationCustomClientFactory factory;

	@RequestMapping("/list")
	public String list() {

		return factory.bla().toString();

//		// Listing Custom resources in a specific namespace
//		JSONObject animalListJSON = new JSONObject(client.customResource(crdBuilder.getCRDContext()).list("default"));
//
//		JSONArray animalItems;
//		try {
//			animalItems = animalListJSON.getJSONArray("items");
//			for (int index = 0; index < animalItems.length(); index++) {
//				JSONObject currentItem = animalItems.getJSONObject(index);
//
//				log.info(currentItem.getJSONObject("metadata").getString("name"));
//			}
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			return e.getMessage();
//		}
//
//		return "list";
	}

}
