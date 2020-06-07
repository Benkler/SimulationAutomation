package org.simulationautomation.kubernetesclient.manager;

import static org.simulationautomation.kubernetesclient.simulation.SimulationProperties.SIMULATION_NAMESPACE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.simulationautomation.kubernetesclient.operator.SimulationOperator;
import org.simulationautomation.nfs.NFSCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.NFSVolumeSource;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.client.KubernetesClient;

@Component
public class ApplicationStartupManager
		implements
			ApplicationListener<ApplicationReadyEvent> {

	static final String IMAGE_PULL_POLICY = "IfNotPresent";
	private static final Logger log = LoggerFactory
		.getLogger(ApplicationStartupManager.class);

	@Autowired
	SimulationOperator simulationOperator;

	@Autowired
	NFSCreator nfsCreator;

	@Autowired
	KubernetesClient client;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {

		clearCluster();

		// nfsCreator.createNFS(SIMULATION_NAMESPACE);
		simulationOperator.init();
		// createpods();

	}

	private void clearCluster() {
		// Remove old pods
		client.pods()
			.inNamespace(SIMULATION_NAMESPACE)
			.delete();

	}

	private void createpods() {

		NFSVolumeSource nfsVolumeSource = new NFSVolumeSource();
		nfsVolumeSource.setPath("/");
		nfsVolumeSource.setServer("10.100.129.240");
		// nfsVolumeSource.setServer("nfs-service");

		Volume volume_1 = new Volume();
		volume_1.setName("nfs-volume");
		volume_1.setNfs(nfsVolumeSource);
		VolumeMount volumeMount_1 = new VolumeMount();
		volumeMount_1.setName("nfs-volume");
		volumeMount_1.setMountPath("/var/nfs");
		// volumeMount_1.setMountPath("/var");

		List<String> args = new ArrayList<>();
		args.add("-c");
		args.add("while true; do date >> /var/nfs/dates.txt; sleep 5; done");

		Container nfsContainer = new ContainerBuilder().withName("app")
			.withImage("alpine")
			.withImagePullPolicy(IMAGE_PULL_POLICY)
			.withVolumeMounts(Collections.singletonList(volumeMount_1))
			.addAllToCommand(Collections.singletonList("/bin/sh"))
			.addAllToArgs(args)
			.build();

		Pod pod = new PodBuilder().withApiVersion("v1")
			.withNewMetadata()
			.withGenerateName("pod-1-using-nfs-")
			.endMetadata()
			.withNewSpec()
			.withVolumes(volume_1)
			.withContainers(nfsContainer)
			.endSpec()
			.build();

		client.pods()
			.inNamespace(SIMULATION_NAMESPACE)
			.create(pod);

		log.info("NFS Pod created");

	}

}
