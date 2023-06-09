/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.mesh;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

@RegisterRestClient(baseUri = "http://spectra:8080")
public interface IdentityResource {
    @GET
    @Path("/identity")
    @Produces(TEXT_PLAIN)
    String generate();
}
