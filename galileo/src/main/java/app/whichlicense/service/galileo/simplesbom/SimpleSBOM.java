/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.galileo.simplesbom;

import com.whichlicense.metadata.identification.license.LicenseIdentificationPipelineTrace;

import java.time.ZonedDateTime;
import java.util.List;

public record SimpleSBOM(String name, String version, long identity, String declaredLicense, String declaredLicenseClass, String discoveredLicense, String discoveredLicenseClass, LicenseIdentificationPipelineTrace license, String type, List<String> ecosystems, String source, ZonedDateTime generated, List<SimpleDependency> directDependencies, List<SimpleDependency> transitiveDependencies) {
}
