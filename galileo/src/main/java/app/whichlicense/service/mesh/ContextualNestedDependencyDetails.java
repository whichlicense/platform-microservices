package app.whichlicense.service.mesh;

import java.util.Map;

public record ContextualNestedDependencyDetails(
        String version,
        VersionType type,
        DependencyKind kind,
        Map<String, String> scans
) {
}
