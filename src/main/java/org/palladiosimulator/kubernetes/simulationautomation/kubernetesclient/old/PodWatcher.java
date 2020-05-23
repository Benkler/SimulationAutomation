package org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.old;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;

public class PodWatcher implements Watcher<Pod> {

	private static final Logger log = LoggerFactory.getLogger(PodWatcher.class);

	@Override
	public void eventReceived(Action action, Pod pod) {
		log.info("Received " + action + " event from Pod with name=" + pod.getMetadata().getName()
				+ " ressource version=" + pod.getMetadata().getResourceVersion());

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

}
