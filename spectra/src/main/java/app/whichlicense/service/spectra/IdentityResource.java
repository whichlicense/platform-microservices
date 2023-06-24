/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.spectra;

import com.whichlicense.metadata.identity.Identity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("/identity")
@RequestScoped
public class IdentityResource {
    private static final String IDENTITY_GETS_COUNTER_NAME = "identityGets";
    private static final String IDENTITY_GETS_COUNTER_DESCRIPTION = "Counts identity GET operations";
    private static final String GETS_TIMER_NAME = "identityGeneration";
    private static final String GETS_TIMER_DESCRIPTION = "Tracks all GET operations";

    @GET
    @Produces(TEXT_PLAIN)
    @Counted(name = IDENTITY_GETS_COUNTER_NAME, absolute = true, description = IDENTITY_GETS_COUNTER_DESCRIPTION)
    @Timed(name = GETS_TIMER_NAME, description = GETS_TIMER_DESCRIPTION, absolute = true)
    public String endpoint() {
        return Identity.toHex(Identity.wrapAndGenerate(Thread.currentThread().threadId()));
    }
}
