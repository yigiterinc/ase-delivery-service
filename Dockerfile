FROM maven:3.8-jdk-11 AS build
RUN ls
WORKDIR /denemeASE

# copy the project files
COPY pom.xml .

# copy your other files
COPY src ./src

# Build a release artifact.
RUN mvn clean package -DskipTests
#COPY src ./Dockerout/src
#COPY pom.xml ./Dockerout
#RUN mvn -f ./Dockerout/pom.xml clean package

FROM bellsoft/liberica-openjdk-alpine:11.0.14
COPY --from=build /denemeASE/target/delivery-service-0.0.1-SNAPSHOT.jar /delivery-service-0.0.1-SNAPSHOT.jar
#COPY --from=build ./Dockerout/target/delivery-service-0.0.1-SNAPSHOT.jar /Users/berkayozerbay/Documents/denemeASE/delivery-service-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","delivery-service-0.0.1-SNAPSHOT.jar"]