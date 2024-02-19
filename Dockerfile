FROM openjdk:21-jdk

COPY ./build/libs/calendar-bot.jar /calendar-bot.jar
COPY ./credentials.json /credentials.json
COPY ./tokens /tokens

EXPOSE 8080
ENV TZ=Europe/Moscow

ENTRYPOINT ["java", "-jar", "/calendar-bot.jar"]