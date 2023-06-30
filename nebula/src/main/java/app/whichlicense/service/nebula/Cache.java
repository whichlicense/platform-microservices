package app.whichlicense.service.nebula;

import com.whichlicense.metadata.identification.license.LicenseIdentificationPipelineTrace;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class Cache {
    private final ConcurrentHashMap<DependencyIdentifier, Set<Long>> identifiers;
    private final ConcurrentHashMap<DependencyIdentifier, UniversalDependencyDetails> universal;
    private final ConcurrentHashMap<Long, ContextualDependencyDetails> contextual;

    public Cache() {
        identifiers = new ConcurrentHashMap<>();
        universal = new ConcurrentHashMap<>();
        contextual = new ConcurrentHashMap<>();
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

    enum DependencyKind {
        DIRECT, TRANSITIVE
    }

    enum VersionType {
        SINGLE, RANGE
    }

    record ContextualNestedDependencyDetails(
            String version,
            VersionType type,
            DependencyKind kind,
            Map<String, String> scans
    ) {
    }

    record ContextualDependencyDetails(
            String locator,
            String source,
            String path,
            String declaredLicense,
            Set<ContextualComplianceDetails> declaredLicenseComplianceStatuses,
            String discoveredLicense,
            LicenseIdentificationPipelineTrace discoveredLicenseTrace,
            Set<ContextualComplianceDetails> discoveredLicenseComplianceStatuses,
            Map<String, ContextualNestedDependencyDetails> dependencies
    ) {
    }
}
