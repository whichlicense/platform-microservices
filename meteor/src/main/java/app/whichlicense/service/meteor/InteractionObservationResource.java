/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.meteor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

import java.io.IOException;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/observation")
@RequestScoped
public class InteractionObservationResource {
    private static final String OBSERVATION_POSTS_COUNTER_NAME = "observationPosts";
    private static final String OBSERVATION_POSTS_COUNTER_DESCRIPTION = "Counts observation POST operations";
    private static final String POSTS_TIMER_NAME = "interactionObservation";
    private static final String POSTS_TIMER_DESCRIPTION = "Tracks all POST operations";

    @Inject
    private ObservedInteractionResource interactionResource;

    @POST
    @Consumes(APPLICATION_JSON)
    @Counted(name = OBSERVATION_POSTS_COUNTER_NAME, absolute = true, description = OBSERVATION_POSTS_COUNTER_DESCRIPTION)
    @Timed(name = POSTS_TIMER_NAME, description = POSTS_TIMER_DESCRIPTION, absolute = true)
    public Response endpoint(Observation observation) throws IOException {
        var objectMapper = new ObjectMapper();
        var session = interactionResource.getSession();
        if (session != null) {
            var json = objectMapper.writeValueAsString(observation);
            session.getAsyncRemote().sendText(json);
        }
        return Response.ok().build();
    }
}
