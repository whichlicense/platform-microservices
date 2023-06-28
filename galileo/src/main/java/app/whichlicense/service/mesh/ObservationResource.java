package app.whichlicense.service.mesh;

import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RegisterRestClient(baseUri = "http://meteor:8080")
public interface ObservationResource {
    @POST
    @Path("/observation")
    @Consumes(APPLICATION_JSON)
    void observeScan(Observation observation);
}
