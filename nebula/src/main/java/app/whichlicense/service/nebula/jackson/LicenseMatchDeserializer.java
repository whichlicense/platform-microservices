/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/jackson-integration.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.nebula.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.whichlicense.metadata.identification.license.LicenseMatch;

import java.io.IOException;
import java.util.Map;

public class LicenseMatchDeserializer extends JsonDeserializer<LicenseMatch> {
    @Override
    @SuppressWarnings("unchecked")
    public LicenseMatch deserialize(JsonParser p, DeserializationContext context) throws IOException {
        var codec = p.getCodec();
        JsonNode node = codec.readTree(p);

        var license = node.get("license").asText();
        var confidence = node.get("confidence").floatValue();
        var algorithm = node.get("algorithm").asText();

        var parametersNode = node.get("parameters");
        Map<String, Object> parameters = codec.treeToValue(parametersNode, Map.class);

        return new LicenseMatchImpl(license, confidence, algorithm, parameters);
    }
}
