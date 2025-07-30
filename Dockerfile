FROM amazoncorretto:17-alpine3.17

WORKDIR /app

COPY app.jar app.jar
ENV JVM_OPTIONS="-Xms512m -Xmx1024m -XX:+UseG1GC -Duser.timezone=Asia/Seoul"

ENTRYPOINT ["sh", "-c", "exec java $JVM_OPTIONS -jar app.jar"]
