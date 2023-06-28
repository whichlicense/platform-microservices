package app.whichlicense.service.mesh;

public record ContextualComplianceDetails(
        String kind,
        String status,
        String explanation
) {
}
