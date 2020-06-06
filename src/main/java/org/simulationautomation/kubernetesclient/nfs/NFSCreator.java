package org.simulationautomation.kubernetesclient.nfs;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.SecurityContext;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * Based on
 * https://matthewpalmer.net/kubernetes-app-developer/articles/kubernetes-volumes-example-nfs-persistent-volume.html
 * 
 * @author Niko Benkler
 *
 */
@Component
public class NFSCreator {

	static final String IMAGE_PULL_POLICY = "IfNotPresent";

	@Autowired
	KubernetesClient client;

	private static final Logger log = LoggerFactory.getLogger(NFSCreator.class);

	public void createNFS(String nameSpace) {
		createNFSService(nameSpace);
		createNFSPod(nameSpace);
		createPersistentVolume(nameSpace);
	}

	private void createPersistentVolume(String nameSpace) {
		// TODO Auto-generated method stub

	}

	private void createNFSPod(String nameSpace) {

		SecurityContext secContext = new SecurityContext();
		secContext.setPrivileged(true);

		ContainerPort port = new ContainerPort();
		port.setContainerPort(2049);
		port.setProtocol("TCP");

		Container nfsContainer = new ContainerBuilder()
			.withName("nfs-server-container")
			.withImage("cpuguy83/nfs-server")
			.withImagePullPolicy(IMAGE_PULL_POLICY)
			.withSecurityContext(secContext)
			.addAllToArgs(Collections.singletonList("/exports"))
			.build();

		Pod pod = new PodBuilder().withApiVersion("v1")
			.withNewMetadata()
			.withGenerateName("nfs-server-pod-")
			.withLabels(Collections.singletonMap("role", "nfs"))
			.endMetadata()
			.withNewSpec()
			.withContainers(nfsContainer)
			.endSpec()
			.build();

		client.pods()
			.inNamespace(nameSpace)
			.create(pod);

		log.info("NFS Pod created");

	}

	private void createNFSService(String nameSpace) {
		Service nfsService = new ServiceBuilder().withApiVersion("v1")
			.withNewMetadata()
			.withName("nfs-service")
			.endMetadata()
			.withNewSpec()
			.withSelector(Collections.singletonMap("role", "nfs"))
			.withClusterIP("10.100.129.240")

			.addNewPort()
			.withName("tcp-8000")
			.withProtocol("TCP")
			.withPort(8000)
			.endPort()

			.addNewPort()
			.withName("tcp-111")
			.withProtocol("TCP")
			.withPort(111)
			.endPort()

			.addNewPort()
			.withName("udp-111")
			.withProtocol("UDP")
			.withPort(111)
			.endPort()

			.addNewPort()
			.withName("tcp-2049")
			.withProtocol("TCP")
			.withPort(2049)
			.endPort()

			.addNewPort()
			.withName("udp-2049")
			.withProtocol("UDP")
			.withPort(2049)
			.endPort()

			.addNewPort()
			.withName("tcp-40001")
			.withProtocol("TCP")
			.withPort(40001)
			.endPort()

			.addNewPort()
			.withName("udp-40001")
			.withProtocol("UDP")
			.withPort(40001)
			.endPort()

			.endSpec()
			.build();

		nfsService = client.services()
			.inNamespace(nameSpace)
			.createOrReplace(nfsService);

		log.info("Ip retrieved");
		for (String ip : nfsService.getSpec()
			.getExternalIPs()) {
			log.info("Ip retrieved=" + ip);
		}

		log.info("Created Service wit name " + nfsService.getMetadata()
			.getName());

		String tcpServiceURL = client.services()
			.inNamespace(nameSpace)
			.withName(nfsService.getMetadata()
				.getName())
			.getURL("tcp-2049");

		log.debug("TCPServiceUrl is=" + tcpServiceURL);

		String udpServiceURL = client.services()
			.inNamespace(nameSpace)
			.withName(nfsService.getMetadata()
				.getName())
			.getURL("udp-111");

		log.info("TCP Service URL", tcpServiceURL);

	}

}
