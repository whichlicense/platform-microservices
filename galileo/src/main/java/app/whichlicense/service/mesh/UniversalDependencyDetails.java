package app.whichlicense.service.mesh;

import java.util.Set;

public record UniversalDependencyDetails(
        String type,
        Set<String> ecosystems
) {
}
