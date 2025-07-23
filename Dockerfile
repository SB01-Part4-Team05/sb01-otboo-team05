FROM amazoncorretto:17-alpine3.17

WORKDIR /app

COPY build/libs/*.jar app.jar
ENV JVM_OPTIONS="-Xms512m -Xmx1024m -XX:+UseG1GC"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JVM_OPTIONS -jar app.jar"]
