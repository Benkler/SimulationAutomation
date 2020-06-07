package org.simulationautomation.kubernetesclient.simulation;

import static org.simulationautomation.kubernetesclient.simulation.SimulationProperties.SIMULATION_KIND;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.simulationautomation.kubernetesclient.crds.Simulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.NFSVolumeSource;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeMount;

public class SimulationPodCreator {
	static final String POD_RESTART_POLICY = "Never";
	static final String IMAGE_PULL_POLICY = "IfNotPresent";
	private static final Logger log = LoggerFactory
		.getLogger(SimulationPodCreator.class);

	public static Pod createSimulationPod(Simulation simulation) {

		Volume inputVolume = createNFSVolumeDefinition("Simu1", "input",
				"Input");
		Volume outpuVolume = createNFSVolumeDefinition("Simu1", "output",
				"Output");

		Container container = createSimulationContainer(simulation);

		Pod pod = new PodBuilder().withApiVersion("v1")

			.withNewMetadata()
			.withGenerateName(simulation.getMetadata()
				.getName() + "-pod-")
			.withNamespace(simulation.getMetadata()
				.getNamespace())
			.withLabels(Collections.singletonMap("app", simulation.getMetadata()
				.getName()))

			.addNewOwnerReference()
			.withController(true)
			.withKind(SIMULATION_KIND)
			.withApiVersion("demo.k8s.io/v1alpha1")
			.withName(simulation.getMetadata()
				.getName())
			.withNewUid(simulation.getMetadata()
				.getUid())
			.endOwnerReference()

			.endMetadata()

			.withNewSpec()
			.withVolumes(inputVolume, outpuVolume)
			.withContainers(container)
			.withRestartPolicy(POD_RESTART_POLICY)
			.endSpec()
			.build();

		return pod;
	}

	private static Container createSimulationContainer(Simulation simulation) {

		// TODO was ist mit dem letzten Backslash
		VolumeMount inputVolumeMount = createVolumeMount("input",
				"/usr/ExperimentData");

		VolumeMount outputVolumeMount = createVolumeMount("output", "/result");

		List<String> args = new ArrayList<>();
		// TODO rename
		args.add(
				"/usr/ExperimentData/model/Experiments/Scalability.experiments");
		args.add("/usr/ExperimentData/model/Experiments/Generated.experiments");
		return new ContainerBuilder().withName("palladiosumlation")
			.withImage("palladiosimulator/palladio-experimentautomation")
			.withImagePullPolicy(IMAGE_PULL_POLICY)
			.withVolumeMounts(inputVolumeMount, outputVolumeMount)
			.addAllToCommand(Collections
				.singletonList("/usr/RunExperimentAutomation.sh"))
			.addAllToArgs(args)
			.build();
	}

	private static Volume createNFSVolumeDefinition(String simulationId,
			String nfsName, String folderName) {

		NFSVolumeSource nfsVolumeSource = new NFSVolumeSource();
		// TODO stimmt der letzte Backslash
		nfsVolumeSource.setPath("/" + simulationId + "/" + folderName + "/");
		nfsVolumeSource.setServer("10.100.224.55"); // TODO use static

		Volume nfs_volume = new Volume();
		nfs_volume.setName(nfsName); // TODO use static
		nfs_volume.setNfs(nfsVolumeSource);

		return nfs_volume;
	}

	private static VolumeMount createVolumeMount(String nfsName,
			String mountPath) {
		VolumeMount volumeMount = new VolumeMount();

		volumeMount.setName(nfsName);
		volumeMount.setMountPath(mountPath);
		return volumeMount;
	}

}
