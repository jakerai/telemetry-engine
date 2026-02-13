# Stage 1: Build & Layering
FROM maven:3.9.6-amazoncorretto-21-al2023 AS build
WORKDIR /src

# Caching dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Building jar
COPY src ./src
RUN mvn clean package -DskipTests

# Extract layers
RUN java -Djarmode=layertools -jar target/*.jar extract

# Stage 2: Runtime
# Using JRE instead of JDK keeps the image smaller and more secure
FROM amazoncorretto:21-al2023-jre
WORKDIR /app

# Install shadow-utils if groupadd/useradd aren't present in your specific JRE image
# (Usually included in AL2023, but good to keep in mind)
RUN groupadd -r devgroup && useradd -r -g devgroup rai

COPY --from=build --chown=rai:devgroup /src/target/dependencies/ ./
COPY --from=build --chown=rai:devgroup /src/target/spring-boot-loader/ ./
COPY --from=build --chown=rai:devgroup /src/target/snapshot-dependencies/ ./
COPY --from=build --chown=rai:devgroup /src/target/application/ ./

USER rai:devgroup

# Best practice for K8s: Allow for custom flags but ensure Java handles signals
ENV JAVA_OPTS="-XX:+UseParallelGC -XX:MaxRAMPercentage=75.0"

# Using 'exec' ensures the Java process receives the SIGTERM from Kubernetes
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]