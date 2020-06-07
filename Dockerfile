FROM adoptopenjdk/openjdk11:alpine-jre
ADD target/simulator-kubernetes.jar app.jar
ADD ExperimentFiles  /usr/ExperimentFiles
ENTRYPOINT ["java","-jar","app.jar"]
EXPOSE 8080