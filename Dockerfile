FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update \
	&& apt-get install -y --no-install-recommends ffmpeg \
	&& rm -rf /var/lib/apt/lists/*

WORKDIR /app
RUN mkdir -p /data/avatars/users /data/moderation/workspaces /data/moderation/jars

COPY --from=build /app/target/redstone-master-web-1.0.0.jar app.jar

ENV JAVA_OPTS="-Dfile.encoding=UTF-8"
VOLUME ["/data"]

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
