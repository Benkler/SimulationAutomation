package org.simulationautomation.kubernetesclient.crds;

import io.fabric8.kubernetes.client.CustomResourceList;

/**
 * POJO for k8s API
 * 
 * @author Niko Benkler
 *
 */
public class SimulationList extends CustomResourceList<Simulation> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1521368070770214997L;
}
