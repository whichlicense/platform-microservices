/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.stellar;

import app.whichlicense.service.stellar.jackson.LicenseIdentificationRequest;
import app.whichlicense.service.stellar.jackson.PipelineStepDescription;
import com.whichlicense.metadata.identification.license.LicenseIdentificationPipeline;
import com.whichlicense.metadata.identification.license.LicenseIdentificationPipelineTrace;
import com.whichlicense.metadata.identification.license.pipeline.PipelineStep;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.whichlicense.metadata.identification.license.LicenseIdentificationPipelineTrace.ofMatchSet;
import static com.whichlicense.metadata.identification.license.LicenseIdentifier.identifyLicenses;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.logging.Logger.getLogger;

@Path("/identify")
@RequestScoped
public class LicenseIdentificationResource {
    private static final String IDENTIFY_POSTS_COUNTER_NAME = "identifyPosts";
    private static final String IDENTIFY_POSTS_COUNTER_DESCRIPTION = "Counts identify POST operations";
    private static final String POSTS_TIMER_NAME = "licenseIdentification";
    private static final String POSTS_TIMER_DESCRIPTION = "Tracks all POST operations";

    @SuppressWarnings("unchecked")
    private List<PipelineStep> fromRawDescription(List<Map<String, Object>> steps) {
        return steps.stream().<PipelineStepDescription>mapMulti((map, consumer) -> {
            if (map.containsKey("operation")) {
                consumer.accept(new PipelineStepDescription((String) map.get("operation"),
                        (Map<String, Object>) map.getOrDefault("parameters", emptyMap())));
            }
        }).map(this::parseStepDescription).toList();
    }

    @SuppressWarnings("unchecked")
    private PipelineStep parseStepDescription(PipelineStepDescription description) {
        var operationParams = description.parameters();
        return switch (description.operation()) {
            case "remove" -> {
                if (operationParams.containsKey("pattern")) {
                    yield PipelineStep.remove(Pattern.compile((String) operationParams.get("pattern")));
                } else {
                    yield PipelineStep.remove((String) operationParams.getOrDefault("text", ""));
                }
            }
            case "replace" -> {
                if (operationParams.containsKey("pattern")) {
                    yield PipelineStep.replace(
                            Pattern.compile((String) operationParams.get("pattern")),
                            (String) operationParams.getOrDefault("replacement", "")
                    );
                } else {
                    yield PipelineStep.replace(
                            (String) operationParams.getOrDefault("text", ""),
                            (String) operationParams.getOrDefault("replacement", "")
                    );
                }
            }
            case "batch" -> {
                if (operationParams.containsKey("steps")) {
                    var steps = (List<Map<String, Object>>) operationParams.get("steps");
                    yield PipelineStep.batch(fromRawDescription(steps));
                } else {
                    yield PipelineStep.batch(emptyList());
                }
            }
            default -> throw new IllegalArgumentException("Unrecognized pipeline step description: " + description);
        };
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Counted(name = IDENTIFY_POSTS_COUNTER_NAME, absolute = true, description = IDENTIFY_POSTS_COUNTER_DESCRIPTION)
    @Timed(name = POSTS_TIMER_NAME, description = POSTS_TIMER_DESCRIPTION, absolute = true)
    public LicenseIdentificationPipelineTrace endpoint(LicenseIdentificationRequest request) {
        var IDENTIFICATION_LOGGER = getLogger("whichlicense.identification");

        var params = request.parameters() == null
                ? new HashMap<String, Object>()
                : request.parameters();

        IDENTIFICATION_LOGGER.finest("Identify: " + request.license());
        if (request.pipeline() == null) {
            var discoveredLicenses = identifyLicenses(request.algorithm(),
                    params, request.license());
            IDENTIFICATION_LOGGER.finest(discoveredLicenses.toString());

            return ofMatchSet(request.algorithm(), params, discoveredLicenses, request.license());
        } else {
            var steps = request.pipeline().steps().stream().map(this::parseStepDescription).toList();
            return LicenseIdentificationPipeline.identifyLicenses(request.pipeline().name(),
                    request.algorithm(), steps, request.license());
        }
    }
}
