package app.whichlicense.service.nebula;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class Cache {
    record UniversalDependencyDetails(
            String type,
            Set<String> ecosystems
    ) {
    }
    record ContextualDependencyDetails(
            String locator,
            String source,
            String path,
            String declaredLicense,
            String declaredLicenseComplianceStatus,
            String discoveredLicense,
            String discoveredLicenseComplianceStatus,
            Map<String, String> dependencies
    ) {
    }
    private ConcurrentHashMap<DependencyIdentifier, Set<Long>> identifiers;
    private ConcurrentHashMap<DependencyIdentifier, UniversalDependencyDetails> universal;
    private ConcurrentHashMap<Long, ContextualDependencyDetails> contextual;

    public Cache() {
        identifiers = new ConcurrentHashMap<>();
        universal = new ConcurrentHashMap<>();
        contextual = new ConcurrentHashMap<>();

        var id = new DependencyIdentifier("placeholder", "1.2.3");

        identifiers.put(id, Set.of(3454543L, 657657L, 231234L));
        universal.put(id, new UniversalDependencyDetails("library", Set.of("npm")));

        contextual.put(3454543L, new ContextualDependencyDetails(
                "https://github.com/some/placeholder",
                "https://github.com/some/placeholder/...",
                "package.json",
                "mit",
                "compliant",
                "mit",
                "compliant",
                Map.of(
                        "babel-1", "2.5.1",
                        "babel-ext-3", "1.2.5"
                )
        ));
        contextual.put(657657L, new ContextualDependencyDetails(
                "https://github.com/some/placeholder",
                "https://github.com/some/placeholder/...",
                "package.json",
                "mit",
                "compliant",
                "mit-v6",
                "not-compliant",
                Map.of(
                        "babel-1", "2.5.1",
                        "babel-ext-3", "1.2.7"
                )
        ));
        contextual.put(231234L, new ContextualDependencyDetails(
                "https://github.com/some/placeholder",
                "https://github.com/some/placeholder/...",
                "package.json",
                "mit",
                "compliant",
                "mit-v6",
                "not-compliant",
                Map.of(
                        "babel-1", "2.5.1",
                        "babel-ext-3", "1.2.7"
                )
        ));
    }

    public ConcurrentHashMap<DependencyIdentifier, Set<Long>> getIdentifiers() {
        return identifiers;
    }

    public ConcurrentHashMap<DependencyIdentifier, UniversalDependencyDetails> getUniversal() {
        return universal;
    }

    public ConcurrentHashMap<Long, ContextualDependencyDetails> getContextual() {
        return contextual;
    }
}
