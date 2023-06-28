package app.whichlicense.service.mesh;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RegisterRestClient(baseUri = "http://nebula:8080/storage")
public interface StorageResource {
    @PUT
    @Path("/identity")
    @Consumes(APPLICATION_JSON)
    void identity(IdentityStorageRequest request);

    @PUT
    @Path("/universal")
    @Consumes(APPLICATION_JSON)
    void universal(UniversalStorageRequest request);

    @PUT
    @Path("/contextual")
    @Consumes(APPLICATION_JSON)
    void contextual(ContextualStorageRequest request);
}
