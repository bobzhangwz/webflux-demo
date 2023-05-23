FROM openjdk:11-jre

WORKDIR /

ARG JAR_FILE=./app/build/libs/app.jar

COPY ${JAR_FILE} app.jar

ADD ./build/otel/aws-opentelemetry-agent.jar /aws-opentelemetry-agent.jar
ENTRYPOINT java -jar -javaagent:/aws-opentelemetry-agent.jar app.jar
# ENTRYPOINT java -jar app.jar
