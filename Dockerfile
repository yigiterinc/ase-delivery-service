FROM bellsoft/liberica-openjdk-alpine-musl:17
#RUN apk update && apk upgrade && apk add netcat-openbsd
RUN mkdir -p /usr/local/cas
ADD target/delivery-service-0.0.1-SNAPSHOT.jar /usr/local/ds/
RUN cd /usr/local/ds
ADD run.sh run.sh
EXPOSE 8080
RUN chmod +x run.sh
CMD ./run.sh
