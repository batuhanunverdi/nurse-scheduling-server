FROM openjdk:17-oracle
VOLUME /tmp
COPY target/nurse-scheduling-server-0.0.1-SNAPSHOT.jar nurse-scheduling-server.jar
ENTRYPOINT ["java","-jar","/nurse-scheduling-server.jar"]
