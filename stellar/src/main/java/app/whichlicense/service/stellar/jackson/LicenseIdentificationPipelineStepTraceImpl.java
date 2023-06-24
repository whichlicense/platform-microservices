/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/jackson-integration.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.stellar.jackson;

import com.whichlicense.metadata.identification.license.LicenseIdentificationPipelineStepTrace;

import java.util.Map;

public record LicenseIdentificationPipelineStepTraceImpl(long step, String operation, Map<String, Object> parameters, Map<String, Float> matches, boolean terminated) implements LicenseIdentificationPipelineStepTrace {
}
