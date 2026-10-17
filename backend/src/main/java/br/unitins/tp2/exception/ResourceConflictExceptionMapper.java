package br.unitins.tp2.exception;

import java.time.OffsetDateTime;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class ResourceConflictExceptionMapper implements ExceptionMapper<ResourceConflictException> {
    @Context UriInfo uri;
    @ConfigProperty(name = "problem.base-url") String baseUrl;

    @Override
    public Response toResponse(ResourceConflictException exception) {
        var problem = new Problem();
        problem.type = baseUrl + "/errors/conflict";
        problem.title = "Conflito de dados";
        problem.status = Response.Status.CONFLICT.getStatusCode();
        problem.detail = exception.getMessage();
        problem.instance = uri == null ? null : uri.getRequestUri().getPath();
        problem.timestamp = OffsetDateTime.now();
        return Response.status(problem.status).type("application/problem+json").entity(problem).build();
    }
}
