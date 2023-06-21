
# 1st stage, build the app
FROM maven:3.9.2-eclipse-temurin-20 as build

WORKDIR /helidon

# Create a first layer to cache the "Maven World" in the local repository.
# Incremental docker builds will always resume after that, unless you update
# the pom
ADD pom.xml .
RUN mvn package -Dmaven.test.skip -Declipselink.weave.skip

# Do the Maven build to create the custom Java Runtime Image
# Incremental docker builds will resume here when you change sources
ADD src src
# Don't generate CDS archive to work around JVM bug https://bugs.openjdk.org/browse/JDK-8274944
RUN mvn package -Pjlink-image -DskipTests -Djlink.image.addClassDataSharingArchive=true
RUN echo "done!"

# 2nd stage, build the final image with the JRI built in the 1st stage

FROM debian:bookworm-slim
WORKDIR /helidon
COPY --from=build /helidon/target/galileo-jri ./
# RUN apt-get update && apt-get install -y ca-certificates
ENTRYPOINT ["/bin/bash", "/helidon/bin/start"]
EXPOSE 8080
EXPOSE 443
