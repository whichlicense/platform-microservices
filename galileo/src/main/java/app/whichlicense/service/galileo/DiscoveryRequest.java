/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.galileo;

import app.whichlicense.service.galileo.jackson.PipelineDescription;

import java.net.URL;
import java.util.Map;

public record DiscoveryRequest(URL url, String algorithm, Map<String, Object> parameters, PipelineDescription pipeline) {
}
