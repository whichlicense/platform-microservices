/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/jackson-integration.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.stellar.jackson;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.whichlicense.metadata.identification.license.LicenseIdentificationPipelineStepTrace;

import java.io.IOException;
import java.util.Map;

public class LicenseIdentificationPipelineStepTraceDeserializer extends JsonDeserializer<LicenseIdentificationPipelineStepTrace> {
    @Override
    @SuppressWarnings("unchecked")
    public LicenseIdentificationPipelineStepTrace deserialize(JsonParser p, DeserializationContext context) throws IOException, JacksonException {
        var codec = p.getCodec();
        JsonNode node = codec.readTree(p);

        var step = node.get("step").asLong();
        var operation = node.get("operation").asText();

        var parametersNode = node.get("parameters");
        Map<String, Object> parameters = codec.treeToValue(parametersNode, Map.class);

        var matchesNode = node.get("matches");
        Map<String, Float> matches = codec.treeToValue(matchesNode, Map.class);

        boolean terminated = node.get("terminated").asBoolean();

        return new LicenseIdentificationPipelineStepTraceImpl(step, operation, parameters, matches, terminated);
    }
}
