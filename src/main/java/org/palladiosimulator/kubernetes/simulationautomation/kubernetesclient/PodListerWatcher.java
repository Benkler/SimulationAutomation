package org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;

@Component
public class PodListerWatcher implements ApplicationListener<ApplicationReadyEvent> {

	@Autowired
	KubernetesClient client;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {

		System.out.println("Application was started");

		List<Pod> podList = client.pods().list().getItems();

		if (podList.isEmpty()) {
			System.out.println("Currently no pods available");

		}

		for (Pod pod : podList) {
			System.out.println("Found Pod with name=" + pod.getMetadata().getName() + " version="
					+ pod.getMetadata().getResourceVersion());
		}

		client.pods().watch(new Watcher<Pod>() {

			@Override
			public void eventReceived(Action action, Pod pod) {
				System.out.println("Received " + action + " event from Pod with name=" + pod.getMetadata().getName()
						+ " version=" + pod.getMetadata().getResourceVersion());

			}

			@Override
			public void onClose(KubernetesClientException cause) {

				// cause != null was due to issue like network connection loss
				if (cause != null) {
					cause.printStackTrace();
					// End pod -> Kubernetes starts a new one
					System.exit(-1);
				}

			}

		});
	}

}
