/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package app.whichlicense.service.mesh;

import com.whichlicense.metadata.identification.license.LicenseMatch;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

@RegisterRestClient(baseUri = "http://stellar:8080")
public interface LicenseIdentificationResource {
    @POST
    @Path("/identify")
    @Consumes(TEXT_PLAIN)
    @Produces(APPLICATION_JSON)
    LicenseMatch identify(String license);
}
