/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.galileo.simplesbom;

import java.util.Map;

public record SimpleDependency(String name, String version, long identity, String declaredLicense, String declaredLicenseClass, String type, DependencyScope scope, String ecosystem, String source, Map<String, String> directDependencies) {
}
