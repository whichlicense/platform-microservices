/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/jackson-integration.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.stellar.jackson;

import java.util.List;
import java.util.Map;

public record PipelineDescription(String name, List<PipelineStepDescription> steps) {
}
