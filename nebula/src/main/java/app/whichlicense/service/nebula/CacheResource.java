package app.whichlicense.service.nebula;

import app.whichlicense.service.nebula.Cache.*;
import com.whichlicense.metadata.identification.license.LicenseIdentificationPipelineTrace;
import com.whichlicense.metadata.identity.Identity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.whichlicense.metadata.identity.Identity.fromHex;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.*;

@Path("/cache")
@RequestScoped
public class CacheResource {
    @Inject
    Cache cache;

    private <T, G> Set<T> groupToSet(
            Map<Long, ContextualDependencyDetails> entries,
            Function<? super ContextualDependencyDetails, ? extends G> classifier,
            BiFunction<? super G, ? super Set<String>, ? extends T> mapper
    ) {
        return entries.entrySet().stream()
                .collect(groupingBy(e -> classifier.apply(e.getValue())))
                .entrySet().stream()
                .map(e -> mapper.apply(e.getKey(), e.getValue().stream()
                        .map(Entry::getKey)
                        .map(Identity::toHex)
                        .collect(toSet())))
                .collect(toSet());
    }

    private Map<String, Set<String>> groupToMap(
            Map<Long, ContextualDependencyDetails> entries,
            Function<? super ContextualDependencyDetails, ? extends String> classifier
    ) {
        return entries.entrySet().stream()
                .filter(e -> !Objects.isNull(classifier.apply(e.getValue())))
                .collect(groupingBy(e -> classifier.apply(e.getValue())))
                .entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().stream()
                        .map(Entry::getKey)
                        .map(Identity::toHex)
                        .collect(toSet()))
                ).collect(toMap(Entry::getKey, Entry::getValue));
    }

    private Map<String, Map<String, Set<SharedDependencyComplianceTail>>> groupCompliance(
            Map<Long, ContextualDependencyDetails> entries,
            Function<? super ContextualDependencyDetails, ? extends Set<ContextualComplianceDetails>> classifier
    ) {
        return entries.entrySet().stream()
                .<Entry<ContextualComplianceDetails, Long>>mapMulti((e, consumer) ->
                        classifier.apply(e.getValue()).forEach(d ->
                                consumer.accept(Map.entry(d, e.getKey()))))
                .collect(groupingBy(e -> e.getKey().kind()))
                .entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().stream()
                        .collect(groupingBy((Entry<ContextualComplianceDetails, Long> e2) -> e2.getKey().status()))
                        .entrySet().stream()
                        .map(e3 -> Map.entry(e3.getKey(), e3.getValue().stream()
                                .collect(groupingBy((Entry<ContextualComplianceDetails, Long> e4) -> e4.getKey().explanation()))
                                .entrySet().stream()
                                .map(e5 -> new SharedDependencyComplianceTail(e5.getKey(), e5.getValue().stream()
                                        .map(Entry::getValue)
                                        .map(Identity::toHex)
                                        .collect(toSet())))
                                .collect(toSet())))
                        .collect(toMap(Entry::getKey, Entry::getValue))))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private Map<String, Set<SharedDependencyReferenceTail>> groupDependencies(
            Map<Long, ContextualDependencyDetails> entries,
            boolean transitive
    ) {
        return entries.entrySet().stream()
                .<Entry<Entry<String, ContextualNestedDependencyDetails>, Long>>mapMulti((e, consumer) ->
                        e.getValue().dependencies().entrySet().forEach(d ->
                                consumer.accept(Map.entry(d, e.getKey()))))
                .collect(groupingBy(Entry::getKey))
                .entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().stream()
                        .map(Entry::getValue).collect(toSet())))
                .collect(groupingBy(e -> e.getKey().getKey()))
                .entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().stream()
                        .map(e2 -> Map.entry(e2.getKey().getValue(), e2.getValue()))
                        .collect(groupingBy(Entry::getKey))))
                .map(e -> Map.entry(e.getKey(), e.getValue().entrySet().stream()
                        .map(e2 -> new SharedDependencyReferenceTail(e2.getKey().version(), e2.getKey().type(), e2.getKey().kind(),
                                e2.getValue().stream()
                                        .flatMap(e3 -> e3.getKey().scans().entrySet().stream())
                                        .collect(groupingBy(Entry::getKey)).entrySet().stream()
                                        .map(e5 -> Map.entry(e5.getKey(), e5.getValue().stream()
                                                .map(Entry::getValue).collect(toSet())))
                                        .collect(toMap(Entry::getKey, Entry::getValue)),
                                e2.getValue().stream()
                                        .flatMap(e3 -> e3.getValue().stream())
                                        .map(Identity::toHex)
                                        .collect(toSet())))
                        .collect(toSet())))
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private SharedDependency lookupDependency(DependencyIdentifier identifier) {
        if (!cache.getIdentifiers().containsKey(identifier)) {
            throw new NoSuchElementException("No entry found for: " + identifier);
        }

        var universal = cache.getUniversal().get(identifier);
        var contextual = cache.getIdentifiers().getOrDefault(identifier, emptySet())
                .stream().map(id -> Map.entry(id, cache.getContextual().get(id)))
                .collect(toMap(Entry::getKey, Entry::getValue));

        record DependencySourceGroup(String locator, String source, String path) {
        }

        return new SharedDependency(
                identifier.name(),
                identifier.version(),
                universal.type(),
                universal.ecosystems(),
                groupToSet(contextual, d -> new DependencySourceGroup(d.locator(), d.source(), d.path()),
                        (g, ids) -> new SharedDependencySource(g.locator, g.source, g.path, ids)),
                groupToMap(contextual, ContextualDependencyDetails::declaredLicense),
                groupCompliance(contextual, ContextualDependencyDetails::declaredLicenseComplianceStatuses),
                groupToMap(contextual, ContextualDependencyDetails::discoveredLicense),
                groupCompliance(contextual, ContextualDependencyDetails::discoveredLicenseComplianceStatuses),
                groupDependencies(contextual, false),
                cache.getIdentifiers().getOrDefault(identifier, emptySet()).stream()
                        .map(Identity::toHex).collect(toSet())
        );
    }

    @GET
    @Path("/dependency")
    @Produces(APPLICATION_JSON)
    public SharedDependency dependency(DependencyIdentifier identifier, @QueryParam("transitive") @DefaultValue("false") boolean transitive) {
        return lookupDependency(identifier);
    }

    @GET
    @Path("/all")
    @Produces(APPLICATION_JSON)
    public Set<SharedDependency> all(@QueryParam("latest") @DefaultValue("false") boolean latest, @QueryParam("transitive") @DefaultValue("false") boolean transitive) {
        return cache.getIdentifiers().keySet().stream().map(this::lookupDependency).collect(toSet());
    }

    private ScanDependency lookupScan(String identity) {
        if (!cache.getContextual().containsKey(fromHex(identity))) {
            throw new NoSuchElementException("No entry found for: " + identity);
        }

        var identifier = cache.getIdentifiers().entrySet().stream()
                .filter(e -> e.getValue().contains(fromHex(identity)))
                .map(Entry::getKey).findFirst().orElse(null);

        if (!cache.getUniversal().containsKey(identifier)) {
            throw new NoSuchElementException("No entry found for: " + identifier);
        }

        var universal = cache.getUniversal().get(identifier);
        var contextual = cache.getContextual().get(fromHex(identity));

        return new ScanDependency(
                identifier.name(),
                identifier.version(),
                universal.type(),
                identity,
                universal.ecosystems(),
                new ScanDependencySource(contextual.locator(), contextual.locator(), contextual.path()),
                contextual.declaredLicense(),
                contextual.declaredLicenseComplianceStatuses().stream()
                        .collect(groupingBy(ContextualComplianceDetails::kind))
                        .entrySet().stream()
                        .map(e -> Map.entry(e.getKey(), e.getValue().stream()
                                .map(e2 -> Map.entry(e2.status(), e2.explanation()))
                                .collect(toMap(Entry::getKey, Entry::getValue))))
                        .collect(toMap(Entry::getKey, Entry::getValue)),
                contextual.discoveredLicense(),
                contextual.discoveredLicenseTrace(),
                contextual.discoveredLicenseComplianceStatuses().stream()
                        .collect(groupingBy(ContextualComplianceDetails::kind))
                        .entrySet().stream()
                        .map(e -> Map.entry(e.getKey(), e.getValue().stream()
                                .map(e2 -> Map.entry(e2.status(), e2.explanation()))
                                .collect(toMap(Entry::getKey, Entry::getValue))))
                        .collect(toMap(Entry::getKey, Entry::getValue)),
                contextual.dependencies()
        );
    }

    @GET
    @Path("/scan")
    @Produces(APPLICATION_JSON)
    public ScanDependency scan(String identity, @QueryParam("transitive") @DefaultValue("false") boolean transitive) {
        return lookupScan(identity);
    }

    @GET
    @Path("/scans")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Set<ScanDependency> scans(List<String> identities, @QueryParam("transitive") @DefaultValue("false") boolean transitive) {
        return identities.stream().map(this::lookupScan).collect(toSet());
    }

    public record SharedDependency(
            String name,
            String version,
            String type,
            Set<String> ecosystems,
            Set<SharedDependencySource> sources,
            Map<String, Set<String>> declaredLicenses,
            Map<String, Map<String, Set<SharedDependencyComplianceTail>>> declaredLicenseComplianceStatuses,
            Map<String, Set<String>> discoveredLicenses,
            Map<String, Map<String, Set<SharedDependencyComplianceTail>>> discoveredLicenseComplianceStatuses,
            Map<String, Set<SharedDependencyReferenceTail>> dependencies,
            Set<String> scans
    ) {
    }

    public record SharedDependencySource(String locator, String source, String path, Set<String> scans) {
    }

    public record SharedDependencyComplianceTail(String explanation, Set<String> scans) {
    }

    public record SharedDependencyReferenceTail(String version, VersionType type, DependencyKind kind,
                                                Map<String, Set<String>> versions, Set<String> scans) {
    }

    public record ScanDependency(
            String name,
            String version,
            String type,
            String identity,
            Set<String> ecosystems,
            ScanDependencySource source,
            String declaredLicense,
            Map<String, Map<String, String>> declaredLicenseComplianceStatuses,
            String discoveredLicense,
            LicenseIdentificationPipelineTrace discoveredLicenseTrace,
            Map<String, Map<String, String>> discoveredLicenseComplianceStatuses,
            Map<String, ContextualNestedDependencyDetails> dependencies
    ) {
    }

    public record ScanDependencySource(String locator, String source, String path) {
    }
}
