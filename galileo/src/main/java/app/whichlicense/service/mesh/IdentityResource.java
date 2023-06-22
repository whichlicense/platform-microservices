package app.whichlicense.service.mesh;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("/identity")
@RegisterRestClient(baseUri = "http://spectra:8080")
public interface IdentityResource {
    @GET
    @Produces(TEXT_PLAIN)
    String generate();
}
