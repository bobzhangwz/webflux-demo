FROM openjdk:11-jre

WORKDIR /

ARG JAR_FILE=./app/build/libs/app.jar

COPY ${JAR_FILE} app.jar

# ADD ./build/otel/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar
# ENTRYPOINT java -jar -javaagent:/opentelemetry-javaagent.jar app.jar
ENTRYPOINT java -jar app.jar
