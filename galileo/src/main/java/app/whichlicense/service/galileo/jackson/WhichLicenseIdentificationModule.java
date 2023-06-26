/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/jackson-integration.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.galileo.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.whichlicense.metadata.identification.license.LicenseIdentificationPipelineStepTrace;
import com.whichlicense.metadata.identification.license.LicenseMatch;

public class WhichLicenseIdentificationModule extends SimpleModule {
    public WhichLicenseIdentificationModule() {
        super("WhichLicenseIdentificationModule", new Version(0, 0, 0, null, null, null));

        var licenseMatchSerializer = new LicenseMatchSerializer();
        addSerializer(LicenseMatch.class, licenseMatchSerializer);

        var licenseMatchDeserializer = new LicenseMatchDeserializer();
        addDeserializer(LicenseMatch.class, licenseMatchDeserializer);

        var licenseIdentificationPipelineStepTraceSerializer = new LicenseIdentificationPipelineStepTraceSerializer();
        addSerializer(LicenseIdentificationPipelineStepTrace.class, licenseIdentificationPipelineStepTraceSerializer);

        var licenseIdentificationPipelineStepTraceDeserializer = new LicenseIdentificationPipelineStepTraceDeserializer();
        addDeserializer(LicenseIdentificationPipelineStepTrace.class, licenseIdentificationPipelineStepTraceDeserializer);
    }
}
