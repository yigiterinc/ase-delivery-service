FROM bellsoft/liberica-openjdk-alpine-musl:17

EXPOSE 8080

CMD echo "********************************************************"
CMD echo "Wait for mongodb to be available"
CMD echo "********************************************************"

CMD echo $MONGODB_STATUS_HOST $MONGODB_STATUS_PORT
CMD while ! nc -z $MONGODB_STATUS_HOST $MONGODB_STATUS_PORT; do
  CMD printf 'mongodb is still not available. Retrying...\n'
  CMD sleep 3
CMD done

CMD echo "********************************************************"
CMD echo "Starting delivery-service"
CMD echo "********************************************************"

CMD java -Dserver.port=$SERVER_PORT \
     -Dspring.data.mongodb.uri=$MONGODB_URI \
     -jar target/delivery-service-0.0.1-SNAPSHOT.jar
