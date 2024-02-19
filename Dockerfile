FROM openjdk:21-jdk

COPY ./build/libs/mpparser.jar /mpparser.jar

EXPOSE 8080
ENV TZ=Europe/Moscow

ENTRYPOINT ["java", "-jar", "/mpparser.jar"]