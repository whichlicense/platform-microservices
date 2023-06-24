/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/jackson-integration.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.stellar.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whichlicense.metadata.identification.license.LicenseIdentificationPipelineStepTrace;
import com.whichlicense.metadata.identification.license.LicenseIdentificationPipelineTrace;
import com.whichlicense.metadata.identification.license.LicenseMatch;

import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new WhichLicenseIdentificationModule());

        var algo = "gaoya";
        Map<String, Object> params = Map.of("some", true);

        var testMatch = new LicenseMatchImpl("test", 95f, algo, params);
        var serializedMatch = mapper.writeValueAsString(testMatch);
        System.out.println(serializedMatch);

        System.out.println(mapper.readValue(serializedMatch, LicenseMatch.class));

        var testTrace = new LicenseIdentificationPipelineStepTraceImpl(1L, "remove-text", Map.of("text", "xxx"), Map.of("mit", 43f), true);
        var serializedTrace = mapper.writeValueAsString(testTrace);
        System.out.println(serializedTrace);

        System.out.println(mapper.readValue(serializedTrace, LicenseIdentificationPipelineStepTrace.class));

        var testUpgradeToTrace = LicenseIdentificationPipelineTrace.ofMatchSet(algo, params, Set.of(testMatch));
        var serializedUpgradeToTrace = mapper.writeValueAsString(testUpgradeToTrace);
        System.out.println(serializedUpgradeToTrace);

        System.out.println(mapper.readValue(serializedUpgradeToTrace, LicenseIdentificationPipelineTrace.class));

        var testEmptyTrace = LicenseIdentificationPipelineTrace.empty("some-name", algo, params);
        var serializedEmptyTrace = mapper.writeValueAsString(testEmptyTrace);
        System.out.println(serializedEmptyTrace);

        System.out.println(mapper.readValue(serializedEmptyTrace, LicenseIdentificationPipelineTrace.class));
    }
}
