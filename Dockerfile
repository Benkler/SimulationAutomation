FROM adoptopenjdk/openjdk11:alpine-jre
ADD target/simulator-kubernetes.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]