/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.galileo;

import app.whichlicense.service.galileo.exceptions.UnsupportedSourceException;
import app.whichlicense.service.galileo.npm.NpmPackageLock;
import app.whichlicense.service.galileo.simplesbom.SimpleDependency;
import app.whichlicense.service.galileo.simplesbom.SimpleSBOM;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.whichlicense.integration.jackson.identity.WhichLicenseIdentityModule;
import com.whichlicense.metadata.identification.license.LicenseClassifier;
import com.whichlicense.metadata.identification.license.LicenseMatch;
import com.whichlicense.metadata.seeker.MetadataMatch;
import com.whichlicense.metadata.seeker.MetadataSeeker;
import com.whichlicense.metadata.sourcing.MetadataSourceResolverProvider;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static app.whichlicense.service.galileo.simplesbom.DependencyScope.COMPILE;
import static app.whichlicense.service.galileo.simplesbom.DependencyScope.TEST;
import static com.whichlicense.metadata.identification.license.HashingAlgorithm.GAOYA;
import static com.whichlicense.metadata.seeker.MetadataSourceType.FILE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.time.Instant.now;
import static java.time.ZoneOffset.UTC;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.logging.Logger.getLogger;
import static java.util.stream.Collectors.toSet;

@Path("/discover")
@RequestScoped
public class DiscoveryResource {
    static Function<java.nio.file.Path, Optional<MetadataMatch>> createMatcher(String glob, MetadataSeeker seeker, java.nio.file.Path root) {
        var compiled = root.getFileSystem().getPathMatcher("glob:" + glob);
        return path -> compiled.matches(path) ? of(new MetadataMatch.FileMatch(root.relativize(path), seeker.getClass())) : empty();
    }

    static Stream<Function<java.nio.file.Path, Optional<MetadataMatch>>> createMatchers(MetadataSeeker seeker, java.nio.file.Path root) {
        return seeker.globs().stream().map(glob -> createMatcher(glob, seeker, root));
    }

    @GET
    @Produces(APPLICATION_JSON)
    public SimpleSBOM endpoint(@QueryParam("url") String rawURL) throws MalformedURLException {
        var inputPath = new URL(rawURL);

        Logger SEEKER_LOGGER = getLogger("whichlicense.seeker");
        Logger MATCHES_LOGGER = getLogger("whichlicense.matches");
        Logger DISCOVERY_LOGGER = getLogger("whichlicense.discovery");
        Logger EXTRACTING_LOGGER = getLogger("whichlicense.extracting");
        Logger DEPENDENCIES_LOGGER = getLogger("whichlicense.dependencies");
        Logger IDENTIFICATION_LOGGER = getLogger("whichlicense.identification");

        final var source = MetadataSourceResolverProvider.loadChain().resolve(inputPath)
                .orElseThrow(() -> new UnsupportedSourceException(rawURL)).path();

        var seekers = ServiceLoader.load(MetadataSeeker.class);
        seekers.iterator().forEachRemaining(seeker -> SEEKER_LOGGER.finest("Registered "
                + seeker.toString().replace("[]", "")));

        var matchers = StreamSupport.stream(seekers.spliterator(), false)
                .filter(seeker -> Objects.equals(seeker.type(), FILE))
                .flatMap(seeker -> createMatchers(seeker, source))
                .toList();

        var discoveredFiles = new ArrayList<java.nio.file.Path>();

        try {
            Files.walkFileTree(source, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs) throws IOException {
                    DISCOVERY_LOGGER.finest("Check " + source.relativize(file));
                    for (var matcher : matchers) {
                        var match = matcher.apply(file);
                        if (match.isPresent()) {
                            MATCHES_LOGGER.info("Discovered " + match.get());
                            discoveredFiles.add(file);
                            break;
                        }
                    }
                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new WhichLicenseIdentityModule());
        //mapper.configure(WRITE_ENUMS_TO_LOWERCASE, true);

        var licenseFileGlob = source.getFileSystem().getPathMatcher("glob:**/LICENSE");
        var lockfileGlob = source.getFileSystem().getPathMatcher("glob:**/package-lock.json");
        Optional<LicenseMatch> discoveredLicense = Optional.empty();

        for (var file : discoveredFiles) {
            var classifier = LicenseClassifier.load();
            if (licenseFileGlob.matches(file)) {
                IDENTIFICATION_LOGGER.finest("Identify LICENSE");
                try {
                    discoveredLicense = classifier.detectLicense(GAOYA, Files.readString(file));
                    IDENTIFICATION_LOGGER.finest(discoveredLicense.toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for (var file : discoveredFiles) {
            if (lockfileGlob.matches(file)) {
                EXTRACTING_LOGGER.finest("Extracting package-lock.json");
                try (var inputStream = Files.newInputStream(file)) {
                    var packageLock = mapper.readValue(inputStream, NpmPackageLock.class);
                    DEPENDENCIES_LOGGER.info("Identified library " + packageLock.name() + "#" + packageLock.version());
                    var packageMetadata = packageLock.packages().get("");

                    var directDependencyNames = Stream.of(packageMetadata.dependencies(), packageMetadata.devDependencies()).flatMap(d -> d.keySet().stream()).collect(toSet());

                    var partitionedDependencies = packageLock.packages().entrySet().stream().filter(entry -> !entry.getKey().isBlank()).map(entry -> {
                        var name = entry.getKey().substring(entry.getKey().lastIndexOf("/") + 1);
                        var metadata = entry.getValue();
                        DEPENDENCIES_LOGGER.finest("Identified dependency " + name + "#" + metadata.version());
                        return new SimpleDependency(name, metadata.version(), 0L, metadata.license(), null, "library", metadata.dev() ? TEST : COMPILE, "npm", source.relativize(file).toString(), metadata.dependencies() == null ? Collections.emptyMap() : metadata.dependencies()); //also add the dev dependencies here in the future
                    }).collect(Collectors.partitioningBy(d -> directDependencyNames.contains(d.name())));

                    var simpleSBOM = new SimpleSBOM(packageLock.name(), packageLock.version(), 0L,
                            packageMetadata.license().toLowerCase(), null, discoveredLicense.map(LicenseMatch::license)
                            .map(l -> l.replaceFirst(".LICENSE", "").toLowerCase()).orElse(null),
                            null, "library", List.of("npm"), source.relativize(file).toString(),
                            now().atZone(UTC), partitionedDependencies.get(true), partitionedDependencies.get(false));

                    return simpleSBOM;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        throw new IllegalStateException("Test");
    }
}
