package app.whichlicense.service.mesh;

import com.whichlicense.metadata.identification.license.LicenseIdentificationPipelineTrace;

import java.util.Map;
import java.util.Set;

public record ContextualDependencyDetails(
        String locator,
        String source,
        String path,
        String declaredLicense,
        Set<ContextualComplianceDetails> declaredLicenseComplianceStatuses,
        String discoveredLicense,
        LicenseIdentificationPipelineTrace discoveredLicenseTrace,
        Set<ContextualComplianceDetails> discoveredLicenseComplianceStatuses,
        Map<String, String> dependencies
) {
}
