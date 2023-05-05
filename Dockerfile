FROM openjdk:11-jre

WORKDIR /

ARG JAR_FILE=./app/build/libs/app.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT java -jar /app.jar
