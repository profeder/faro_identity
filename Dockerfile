FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/faro-identity-0.1.0-SNAPSHOT.jar app.jar
USER 10001
EXPOSE 9000
ENTRYPOINT ["java","-jar","/app/app.jar"]
