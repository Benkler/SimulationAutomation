package org.palladiosimulator.kubernetes.simulationautomation.kubernetesclient.api;

import java.util.Map;

public interface ICustomResourceBuilder {

	/**
	 * Create custom ressource accroding to crDefinition in
	 * "simulation_cr_template.yml" with specified name in namespace
	 * 
	 * @param name
	 * @param namespace
	 * @return
	 */
	Map<String, Object> createCustomResource(String name, String namespace);

}