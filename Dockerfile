FROM bellsoft/liberica-openjdk-alpine:17
RUN mkdir -p /usr/local/cas
ADD target/delivery-service-0.0.1-SNAPSHOT.jar /usr/local/ds
ADD run.sh run.sh
EXPOSE 8080
CMD ./run.sh
