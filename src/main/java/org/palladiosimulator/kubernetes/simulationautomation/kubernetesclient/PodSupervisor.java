package org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;

@Component
public class PodSupervisor implements ApplicationListener<ApplicationReadyEvent> {

	private static final Logger log = LoggerFactory.getLogger(PodSupervisor.class);

	@Autowired
	KubernetesClient client;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {

		log.info("Application was started");
		logAvailablePods();
		// Get notify by k8s, when anything happens to a pod
		client.pods().watch(new PodWatcher());
	}

	public void logAvailablePods() {

		List<Pod> podList = client.pods().list().getItems();

		if (podList.isEmpty()) {
			log.warn("Currently no pods available");
		}

		for (Pod pod : podList) {
			log.info("Found Pod with name=" + pod.getMetadata().getName() + " version="
					+ pod.getMetadata().getResourceVersion() + "\n");
		}

	}

	public PodList getAvailablePods() {

		return client.pods().list();
	}

}
