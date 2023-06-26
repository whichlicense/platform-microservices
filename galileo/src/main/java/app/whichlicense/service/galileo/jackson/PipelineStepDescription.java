/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/jackson-integration.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.galileo.jackson;

import java.util.Map;

public record PipelineStepDescription(String operation, Map<String, Object> parameters) {
}
