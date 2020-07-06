package org.kubernetessimulationcreator;

import java.util.Map;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class ModelLoader {



  public void test() {


    // Register the XMI resource factory for the .website extension

    Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
    Map<String, Object> m = reg.getExtensionToFactoryMap();
    m.put("experiments", new XMIResourceFactoryImpl());

    ResourceSet resSet = new ResourceSetImpl();

    Resource resource = resSet.getResource(
        URI.createURI("src/main/resources/Model/Experiments/Capacity.experiments"), true);



    EObject obj = resource.getContents().get(0);


    System.out.println("TEST");

  }

}
