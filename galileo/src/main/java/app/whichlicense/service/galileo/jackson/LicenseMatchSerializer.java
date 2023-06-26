/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/jackson-integration.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.galileo.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.whichlicense.metadata.identification.license.LicenseMatch;

import java.io.IOException;

public class LicenseMatchSerializer extends JsonSerializer<LicenseMatch> {
    @Override
    public void serialize(LicenseMatch value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("license", value.license());
        gen.writeNumberField("confidence", value.confidence());
        gen.writeStringField("algorithm", value.algorithm());
        gen.writeObjectField("parameters", value.parameters());
        gen.writeEndObject();
    }
}
