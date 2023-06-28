/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/jackson-integration.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.nebula.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.whichlicense.metadata.identification.license.LicenseIdentificationPipelineStepTrace;

import java.io.IOException;

public class LicenseIdentificationPipelineStepTraceSerializer extends JsonSerializer<LicenseIdentificationPipelineStepTrace> {
    @Override
    public void serialize(LicenseIdentificationPipelineStepTrace value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("step", value.step());
        gen.writeStringField("operation", value.operation());
        gen.writeObjectField("parameters", value.parameters());
        gen.writeObjectField("matches", value.matches());
        gen.writeBooleanField("terminated", value.terminated());
        gen.writeEndObject();
    }
}
