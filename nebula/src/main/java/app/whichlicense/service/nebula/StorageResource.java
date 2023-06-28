package app.whichlicense.service.nebula;

import app.whichlicense.service.nebula.Cache.ContextualDependencyDetails;
import app.whichlicense.service.nebula.Cache.UniversalDependencyDetails;
import app.whichlicense.service.nebula.jackson.WhichLicenseIdentificationModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import java.util.TreeSet;

import static com.whichlicense.metadata.identity.Identity.fromHex;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/storage")
@RequestScoped
public class StorageResource {
    @Inject
    Cache cache;

    @PUT
    @Path("/identity")
    @Consumes(APPLICATION_JSON)
    public void identity(IdentityStorageRequest request) {
        cache.getIdentifiers().putIfAbsent(request.identifier, new TreeSet<>());
        cache.getIdentifiers().get(request.identifier).add(fromHex(request.identity));
    }

    @PUT
    @Path("/universal")
    @Consumes(APPLICATION_JSON)
    public void universal(UniversalStorageRequest request) {
        cache.getUniversal().putIfAbsent(request.identifier, request.details);
    }

    @PUT
    @Path("/contextual")
    @Consumes(APPLICATION_JSON)
    public void contextual(String raw) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        mapper.registerModule(new WhichLicenseIdentificationModule());
        var request = mapper.readValue(raw, ContextualStorageRequest.class);
        cache.getContextual().put(fromHex(request.identity), request.details);
    }

    record IdentityStorageRequest(DependencyIdentifier identifier, String identity) {
    }

    record UniversalStorageRequest(DependencyIdentifier identifier, UniversalDependencyDetails details) {
    }

    record ContextualStorageRequest(String identity, ContextualDependencyDetails details) {
    }
}
