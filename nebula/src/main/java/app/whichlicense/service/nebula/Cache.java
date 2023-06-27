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
    record ContextualComplianceDetails(
            String kind,
            String status,
            String explanation
    ) {
    }
    record ContextualDependencyDetails(
            String locator,
            String source,
            String path,
            String declaredLicense,
            Set<ContextualComplianceDetails> declaredLicenseComplianceStatuses,
            String discoveredLicense,
            Set<ContextualComplianceDetails> discoveredLicenseComplianceStatuses,
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
                Set.of(new ContextualComplianceDetails("osadl", "compliant", "...")),
                "mit",
                Set.of(new ContextualComplianceDetails("osadl", "compliant", "...")),
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
                Set.of(new ContextualComplianceDetails("osadl", "compliant", "...")),
                "mit-v6",
                Set.of(new ContextualComplianceDetails("mixed", "compliant", "...")),
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
                Set.of(new ContextualComplianceDetails("osadl", "compliant", "...")),
                "mit-v6",
                Set.of(new ContextualComplianceDetails("mixed", "compliant", "...2...")),
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
