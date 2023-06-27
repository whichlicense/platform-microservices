package app.whichlicense.service.nebula;

import app.whichlicense.service.nebula.Cache.ContextualDependencyDetails;
import com.whichlicense.metadata.identity.Identity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

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
                .collect(groupingBy(e -> classifier.apply(e.getValue())))
                .entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().stream()
                        .map(Entry::getKey)
                        .map(Identity::toHex)
                        .collect(toSet()))
                ).collect(toMap(Entry::getKey, Entry::getValue));
    }

    private Map<String, Set<SharedDependencyReferenceTail>> groupDependencies(
            Map<Long, ContextualDependencyDetails> entries
    ) {
        return entries.entrySet().stream()
                .<Entry<Entry<String, String>, Long>>mapMulti((e, consumer) ->
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
                        .map(e2 -> new SharedDependencyReferenceTail(e2.getKey(), e2.getValue().stream()
                                .flatMap(e3 -> e3.getValue().stream()).map(Identity::toHex)
                                .collect(toSet())))
                        .collect(toSet())))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    @GET
    @Path("/dependency")
    @Produces(APPLICATION_JSON)
    public SharedDependency endpoint(DependencyIdentifier reference) {
        if (!cache.getIdentifiers().containsKey(reference)) {
            throw new NoSuchElementException("No entry found for: " + reference);
        }

        var universal = cache.getUniversal().get(reference);
        var contextual = cache.getIdentifiers().getOrDefault(reference, emptySet())
                .stream().map(id -> Map.entry(id, cache.getContextual().get(id)))
                .collect(toMap(Entry::getKey, Entry::getValue));

        record DependencySourceGroup(String locator, String source, String path) {
        }

        return new SharedDependency(
                reference.identifier(),
                reference.version(),
                universal.type(),
                universal.ecosystems(),
                groupToSet(contextual, d -> new DependencySourceGroup(d.locator(), d.source(), d.path()),
                        (g, ids) -> new SharedDependencySource(g.locator, g.source, g.path, ids)),
                groupToMap(contextual, ContextualDependencyDetails::declaredLicense),
                groupToMap(contextual, ContextualDependencyDetails::declaredLicenseComplianceStatus),
                groupToMap(contextual, ContextualDependencyDetails::discoveredLicense),
                groupToMap(contextual, ContextualDependencyDetails::discoveredLicenseComplianceStatus),
                groupDependencies(contextual),
                cache.getIdentifiers().getOrDefault(reference, emptySet()).stream()
                        .map(Identity::toHex).collect(toSet())
        );
    }

    public record SharedDependency(
            String name,
            String version,
            String type,
            Set<String> ecosystems,
            Set<SharedDependencySource> sources,
            Map<String, Set<String>> declaredLicenses,
            Map<String, Set<String>> declaredLicenseComplianceStatuses,
            Map<String, Set<String>> discoveredLicenses,
            Map<String, Set<String>> discoveredLicenseComplianceStatuses,
            Map<String, Set<SharedDependencyReferenceTail>> dependencies,
            Set<String> scans
    ) {
    }

    public record SharedDependencySource(String locator, String source, String path, Set<String> scans) {
    }

    public record SharedDependencyReferenceTail(String version, Set<String> scans) {
    }
}
