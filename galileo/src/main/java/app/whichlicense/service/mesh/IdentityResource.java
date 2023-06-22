package app.whichlicense.service.mesh;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

@RegisterRestClient(baseUri = "http://spectra")
public interface IdentityResource {
    @GET
    @Path("/identity")
    @Produces(TEXT_PLAIN)
    String generate();
}
