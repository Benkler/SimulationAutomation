package org.simulationautomation.kubernetesclient.api;

import java.util.Map;

public interface ICustomResourceBuilder {

	Map<String, Object> createCustomResource(String name, String namespace);

}