From openjdk:17-ea-jdk

run mkdir /app
workdir /app

ADD ./build/libs/*.jar /app/app.jar

EXPOSE 8787

ENTRYPOINT ["java","-jar","app.jar"]