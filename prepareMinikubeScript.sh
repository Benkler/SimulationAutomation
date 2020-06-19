 #!/bin/bash
 
  eval $(minikube docker-env)
  
  echo "---------------Starting to prepare minikube cluster-------------------"
  
  minikube delete --all && minikube start --driver=kvm2 --cpus 2 --memory 4096 
  
  docker pull cpuguy83/nfs-server
  
  docker pull palladiosimulator/palladio-experimentautomation
  
  docker pull adoptopenjdk/openjdk11:alpine-jre
  
  
