package org.palladiosimulator.kubernetes.simulationautomation.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.fabric8.kubernetes.client.KubernetesClient;

@RestController
@Component
public class WelcomeController {


	@Autowired
	KubernetesClient client;

	@RequestMapping("/greeting")
	public String greeting() {
		System.out.println("RestEndpoint triggered");
		return "Welcome! There are " + client.pods().list().getItems().size() + " pods registered in namespace:"
				+ client.getNamespace();
	}

}
