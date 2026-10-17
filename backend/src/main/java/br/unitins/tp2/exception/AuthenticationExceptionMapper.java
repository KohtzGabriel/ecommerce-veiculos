package br.unitins.tp2.exception;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException> {
    @Context UriInfo uri;

    @Override
    public Response toResponse(AuthenticationException exception) {
        var problem = new Problem();
        problem.type = "/errors/unauthorized";
        problem.title = "Não autenticado";
        problem.status = Response.Status.UNAUTHORIZED.getStatusCode();
        problem.detail = exception.getMessage();
        problem.instance = uri == null ? null : uri.getRequestUri().getPath();
        problem.timestamp = java.time.OffsetDateTime.now();
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(problem)
                .type("application/problem+json")
                .build();
    }
}
