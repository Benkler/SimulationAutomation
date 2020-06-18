 #!/bin/bash
 

 
 eval $(minikube docker-env)
 
 echo "Starting to build and deploy k8s-simulation-automation"
 
 echo "-----------------mvn clean install--------------------------------"
 
 mvn clean install
 
 echo "-----------------Docker Build-------------------------------------"
 
 docker build -t simulationautomation-k8s:1.0 .
 
 echo "-----------------kubectl apply clusterRole------------------------"
 
 kubectl apply -f src/main/resources/clusterRole.yml 
 
  
 echo "-----------------kubectl apply service----------------------------"
 
 kubectl apply -f src/main/resources/service.yml 
 
  
 echo "------------kubectl delete deployment simulationAutomation---------"
 
 kubectl delete deployments simulationautomation-k8s
 
 echo "-----------------kubectl apply deployment--------------------------"
 
 kubectl apply -f src/main/resources/deployment.yml 
 
 sleep 2
 
 echo "-----------------kubectl get deployments---------------------------"
 
 kubectl get deployments
 
  echo "-----------------kubectl get pods---------------------------------"
 
 kubectl get pods
