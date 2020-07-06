# SimulationAutomation

## Preface

This project contains a tool to automatically run Palladio experiments in parallel by separately deploying each experiment on a Kubernetes cluster. The  [ExperimentAutomation][ExperimentAutomation]  is already provided as docker container. The aim of this project is to write an intelligent RestClient that is able to receive the experiment files, set up and start a Kubernetes pods containing the mentioned docker container and finally collect the simulation data. 

Therefore, the following steps were necessary:

1. Create a java-based REST-Client which also runs in a pod
2. Use an existing Kubernetes-Client to access the Kubernetes-Api
3. Choose a methodology to exchange and persist data between the pods and the client
4. Define a way to transmit simulation data

#### 

#### Prerequisites

Good understanding of the basic principles of [Kubernetes][] (especially CurstomResources and file storage).

The setup:

* Minikube v1.11.0 with kvm2 Hypervisor 
* Ubuntu 20.04 LTS

Hints:

* kvm2 needs at least 4GB Ram (minikube start --driver=kvm2 --cpus 2 --memory 4096 )
* Following commands need to be executed 
  * kubectl apply -f pathToClusterRoleFile/clusterRole.yml  -> rights for rest client
  * kubectl apply -f pathToServiceFile/service.yml  -> Expose rest service



## Basic Concepts

Basic Concepts of the kubernetes client. 

### Fabric-8 Kubernetes Client

The [Fabric-8 Kubrenetes Client][] provides access to the full Kubernetes REST API via fluent DSL. 

It is used to:

* Create Pods for Simulation 
* Create Docker Container Specifications for pods
* Create Simulation as Custom Resource Definition to encapsulate Simulation Pods
* Create Watchers for Events thrown by Pods and CRDs  (Simulation finished, pod crashed...)
* Create Watchers for Logs (Write Pod logs into persistent files)
* Create NFS within a Pod (Test Environment only!)

In general, the Fabric-8 Java Client is used  to manage the Kubernetes environment without using .yaml files and console commands.  For example, a Pod can be create via:

    Pod pod = new PodBuilder().
      withApiVersion("v1").
      withNewMetadata().
         withGenerateName("name")
        .withNamespace("default")
        .withLabels(app,label)
        .addNewOwnerReference()
           .withController(true)
           .withKind("Pod")
           ...
        .endOwnerReference()
      .endMetadata()
      .withNewSpec()
         .withContainers(container)
         .withRestartPolicy(POD_RESTART_POLICY)
      .endSpec()
    .build();`
And very easily created/deployed on the cluster via a respective client:

`client.pods().inNamespace("default").create(pod);`

Therefore, it is possible to dynamically create a pod, if someone triggers the Rest-API. The Fabric-8 Java Client provides many more useful features to observe the Kubernetes Cluster in charge and dynamically react to changes.

### Pods

* Each experiment runs in a docker container with following docker image
  *   **`palladiosimulator/palladio-experimentautomation`**
* Start simulation with existing script of [ExperimentAutomation][ExperimentAutomation]  
  * **`RunExperimentAutomation.sh`**
* Data Exchange:
  * Define Input- and Output Folder for simulation files (Input) and simulation results (Output) as VolumeDefinition in Kubrenetes -> Both folders are again mounted into the palladio simulation docker container
  * Input folder: palladio expects experiment data in `/usr/ExperimentData`
  * Ouput: palladio writes results into `/result`
  * General structure for data exchange -> See "Data Exchange: Network File System"

### Simulation CustomResourceDefinition (CRD)

With CustomResourceDefinitions, Kubrenetes provides a concept to create a customized object that can be configured and deployed to the Kubernetes cluster. In this project, a  pod containing the palladio simulation container is always created alongside a simulation CRD to control and persist simulations. 

First, a Simulation CRD prepares the folder structure. This includes to copy the simulation experiment files to the desired destination (which is mounted into the pod). 

Second, a Simulation CRD can be easily persisted as JSON and written to the NFS. Therefore, it lasts longer than the lifecycle of a pod. Pods get deleted in order to save resources (memory) as soon as they are finished and wrote the results to the respective output folder. Also, this allows the client and even the Kubernetes Environment to crash. In case of a restart, the NFS can be traversed and the persisted simulation CRDs can be restored. Hence, the results of previous simulations can still be accessed.

Third, CRDs are open for extensions. New attributes can be defined very easily.

Summarised:

* SimulationCRD provides skeleton for a simulation pods
* SimulationCRDs are persisted and restored after a crash
* SimulationCRDs are the "entrypoint" to the respective simulation 
* Access simulation by their unique name (simulation-"uuid")

### Data Exchange: Network File System

A Container's file system lives only as long as the Container does. So when a Container terminates and restarts, filesystem changes are lost. For more consistent storage that is independent of the Container and not limited to the lifecycle of a Pod, Volumes need to be used.

The difficulty for this application is the "read/write-many relationship" between the client and the simulation pods. The simulation client has to provide the (via REST received) simulation data for the pods. Once finished, the pods need to persist the simulation data at a destination where the simulation client has access. In short, it is necessary to create an environment which both, simulation client and simulation pod can access. 

Most of the supported [VolumePlugins][] do not provide the ReadWriteMany-Feature. Of those who are supporting it, NFS is a straightforward solution. 

In the case of the simulation client, we have the following structure:

* Root Path of the simulation Client: `/usr/Simulation`
  * The NFS path `"/"`is mounted to this path 
* Root Path of each simulation: `/usr/Simulation/simulation-"uuid"`
  * "uuid" is replaced by an integer (eg..: `simulation-7f90b1e143fb458eb14d0077114dd51a`)
  * Simulation pods do not have access to this folder (only to sub folder Input/Ouput)
* Input Path of each simulation: `/usr/Simulation/simulation-"uuid"/Input`
  * This folder is mounted into the the pod
  * During docker container creation, this folder is mounted into the docker container at path `/usr/ExperimentData`
* Output Path of each simulation: `/usr/Simulation/simulation-"uuid"/Output`
  * This folder is mounted into the the pod
  * During docker container creation, this folder is mounted into the docker container at path `/result`
* LogFile of a simulation pod: `/usr/Simulation/simulation-"uuid"/log.txt`
  * LogWatcher writes the log files of a simulation pod into this file
  * Can be accessed after pod has been deleted!
* MetaData File of a simulation: `/usr/Simulation/simulation-"uuid"/ExperimentData.json`
  * meta data file is updated whenever the simulation watcher captures an event that changes the status of the simulation
  * also used to restore simulations in case of a crash

It is important to mention that a simulation pod cannot access the folder structure of another pod, not to mention the log file and metadata file of the accompanying simulation. 

### Watcher for Pods and Simulation CRDs

### Log Files

### 



## Decisions during the Design Process

The following paragraph provides the most critical design decision that were made during the design process.

#### CustomResourceDefinition: Simulation

#### Data Persistence: Network File System

#### Rest-Api

#### Lifecycle of Pods and Simulations







### Links

[ExperimentAutomation]: https://github.com/PalladioSimulator/Palladio-Build-Docker/tree/master/PalladioExperimentAutomation
[Kubernetes]: https://kubernetes.io/de/docs/home/
[Docker]: https://docs.docker.com/
[Fabric-8 Kubrenetes Client]: https://github.com/fabric8io/kubernetes-client
[VolumePlugins]: https://kubernetes.io/docs/concepts/storage/persistent-volumes/