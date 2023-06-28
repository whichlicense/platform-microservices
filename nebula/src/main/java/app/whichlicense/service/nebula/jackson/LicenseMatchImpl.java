/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/jackson-integration.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.nebula.jackson;

import com.whichlicense.metadata.identification.license.LicenseMatch;

import java.util.Map;

public record LicenseMatchImpl(String license, float confidence, String algorithm, Map<String, Object> parameters) implements LicenseMatch {
}
