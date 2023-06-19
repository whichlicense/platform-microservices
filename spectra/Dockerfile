
# 1st stage, build the app
FROM maven:3.9.1-eclipse-temurin-20 as build

WORKDIR /helidon

# Create a first layer to cache the "Maven World" in the local repository.
# Incremental docker builds will always resume after that, unless you update
# the pom
ADD pom.xml pom.xml
RUN mvn package -Dmaven.test.skip -Declipselink.weave.skip -DskipOpenApiGenerate

# Do the Maven build!
# Incremental docker builds will resume here when you change sources
ADD src src
RUN mvn package -DskipTests

RUN echo "done!"

# 2nd stage, build the runtime image
FROM openjdk:20-jdk-slim
WORKDIR /helidon

# Copy the binary built in the 1st stage
COPY --from=build /helidon/target/spectra.jar ./
COPY --from=build /helidon/target/libs ./libs

CMD ["java", "--enable-preview", "-jar", "spectra.jar"]

EXPOSE 8080
