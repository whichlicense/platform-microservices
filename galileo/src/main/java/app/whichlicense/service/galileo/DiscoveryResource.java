/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.galileo;

import app.whichlicense.service.galileo.exceptions.UnsupportedSourceException;
import app.whichlicense.service.galileo.jackson.LicenseIdentificationRequest;
import app.whichlicense.service.galileo.jackson.WhichLicenseIdentificationModule;
import app.whichlicense.service.galileo.npm.NpmPackageLock;
import app.whichlicense.service.mesh.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.whichlicense.integration.jackson.identity.WhichLicenseIdentityModule;
import com.whichlicense.metadata.identification.license.LicenseIdentificationPipelineTrace;
import com.whichlicense.metadata.identity.Identity;
import com.whichlicense.metadata.seeker.MetadataMatch;
import com.whichlicense.metadata.seeker.MetadataSeeker;
import com.whichlicense.metadata.sourcing.MetadataSourceResolverProvider;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static app.whichlicense.service.mesh.DependencyKind.DIRECT;
import static app.whichlicense.service.mesh.DependencyKind.TRANSITIVE;
import static app.whichlicense.service.mesh.VersionType.RANGE;
import static app.whichlicense.service.mesh.VersionType.SINGLE;
import static com.whichlicense.metadata.seeker.MetadataSourceType.FILE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static java.time.Instant.now;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.emptySet;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.logging.Logger.getLogger;
import static java.util.stream.Collectors.toSet;

@Path("/discover")
@RequestScoped
public class DiscoveryResource {
    @Inject
    @RestClient
    private IdentityResource identityResource;
    @Inject
    @RestClient
    private LicenseIdentificationResource licenseIdentificationResource;
    @Inject
    @RestClient
    private StorageResource storageResource;
    @Inject
    @RestClient
    private ObservationResource observationResource;

    static Function<java.nio.file.Path, Optional<MetadataMatch>> createMatcher(String glob, MetadataSeeker seeker, java.nio.file.Path root) {
        var compiled = root.getFileSystem().getPathMatcher("glob:" + glob);
        return path -> compiled.matches(path) ? of(new MetadataMatch.FileMatch(root.relativize(path), seeker.getClass())) : empty();
    }

    static Stream<Function<java.nio.file.Path, Optional<MetadataMatch>>> createMatchers(MetadataSeeker seeker, java.nio.file.Path root) {
        return seeker.globs().stream().map(glob -> createMatcher(glob, seeker, root));
    }

    private static final Pattern VALID_SEMVER = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

    static Map<String, ContextualNestedDependencyDetails> mapDependencies(Map<String, String> dependencies) {
        return dependencies.entrySet().stream()
                .map(d -> Map.entry(d.getKey(), new ContextualNestedDependencyDetails(d.getValue(),
                        VALID_SEMVER.matcher(d.getValue()).matches() ? SINGLE : RANGE, DIRECT, new HashMap<>())))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (o, n) -> n));
    }

    static Map<String, ContextualNestedDependencyDetails> mapNestedDependencies(Map<Boolean, List<Entry<String, DependencyIdentifier>>> dependencies) {
        return Stream.concat(dependencies.get(true).stream().map(e -> Map.entry(true, e)),
                dependencies.get(false).stream().map(e -> Map.entry(false, e)))
                .map(e -> Map.entry(e.getValue().getValue().name(),
                        new ContextualNestedDependencyDetails(e.getValue().getValue().version(),
                                VALID_SEMVER.matcher(e.getValue().getValue().version()).matches() ? SINGLE : RANGE,
                                e.getKey() ? DIRECT : TRANSITIVE, Map.of(e.getValue().getValue().version(), e.getValue().getKey())
                ))).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (o, n) -> n));
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(TEXT_PLAIN)
    public String endpoint(DiscoveryRequest request) throws IOException {
        Logger SEEKER_LOGGER = getLogger("whichlicense.seeker");
        Logger MATCHES_LOGGER = getLogger("whichlicense.matches");
        Logger DISCOVERY_LOGGER = getLogger("whichlicense.discovery");
        Logger EXTRACTING_LOGGER = getLogger("whichlicense.extracting");
        Logger DEPENDENCIES_LOGGER = getLogger("whichlicense.dependencies");

        final var source = MetadataSourceResolverProvider.loadChain().resolve(request.url(), new ConfigurationStub())
                .orElseThrow(() -> new UnsupportedSourceException(request.toString())).path();

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
        mapper.registerModule(new WhichLicenseIdentificationModule());
        //mapper.configure(WRITE_ENUMS_TO_LOWERCASE, true);

        var licenseFileGlob = source.getFileSystem().getPathMatcher("glob:**/LICENSE");
        var lockfileGlob = source.getFileSystem().getPathMatcher("glob:**/package-lock.json");
        Optional<LicenseIdentificationPipelineTrace> discoveredLicense = Optional.empty();

        for (var file : discoveredFiles) {
            if (licenseFileGlob.matches(file)) {
                discoveredLicense = Optional.of(mapper.readValue(licenseIdentificationResource.identify(new LicenseIdentificationRequest(
                        Files.readString(file), request.algorithm() == null ? "gaoya" : request.algorithm(),
                        request.parameters() == null ? new HashMap<>() : request.parameters(), request.pipeline()
                )), LicenseIdentificationPipelineTrace.class));
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
                        var identity = identityResource.generate();
                        var identifier = new DependencyIdentifier(name, metadata.version());

                        storageResource.identity(new IdentityStorageRequest(identifier, identity));
                        storageResource.universal(new UniversalStorageRequest(identifier, new UniversalDependencyDetails("library", Set.of("npm"))));
                        storageResource.contextual(new ContextualStorageRequest(identity, new ContextualDependencyDetails(null, null,
                                source.relativize(file).toString(), metadata.license(), emptySet(), metadata.license(), LicenseIdentificationPipelineTrace.
                                empty("auto", "gaoya", new HashMap<>(), ""), emptySet(), metadata.dependencies() == null
                                ? Collections.emptyMap() : mapDependencies(metadata.dependencies()))));

                        return Map.entry(identity, identifier);
                    }).collect(Collectors.partitioningBy(d -> directDependencyNames.contains(d.getValue().name())));

                    var identity = identityResource.generate();
                    var identifier = new DependencyIdentifier(packageLock.name(), packageLock.version());

                    storageResource.identity(new IdentityStorageRequest(identifier, identity));
                    storageResource.universal(new UniversalStorageRequest(identifier, new UniversalDependencyDetails("library", Set.of("npm"))));
                    storageResource.contextual(new ContextualStorageRequest(identity, new ContextualDependencyDetails(null, null,
                            source.relativize(file).toString(), packageMetadata.license().toLowerCase(), emptySet(),
                            discoveredLicense.map(LicenseIdentificationPipelineTrace::license).map(l -> l.replaceFirst(".LICENSE", "")
                                    .toLowerCase()).orElse(null), discoveredLicense.get(), emptySet(),
                            mapNestedDependencies(partitionedDependencies))));

                    observationResource.observeScan(new Observation(3, new ObservedScan(identity)));

                    return identity;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        throw new IllegalStateException("No primary metadata source found");
    }
}
