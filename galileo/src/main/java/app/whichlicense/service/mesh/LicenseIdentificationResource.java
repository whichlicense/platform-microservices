/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.mesh;

import app.whichlicense.service.galileo.jackson.LicenseIdentificationRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RegisterRestClient(baseUri = "http://stellar:8080")
public interface LicenseIdentificationResource {
    @POST
    @Path("/identify")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    String identify(LicenseIdentificationRequest request);
}
