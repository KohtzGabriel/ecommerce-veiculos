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
public class CepUnavailableExceptionMapper implements ExceptionMapper<CepUnavailableException> {

    @ConfigProperty(name = "problem.base-url")
    String baseUrl;

    @Context
    UriInfo uri;

    @Override
    public Response toResponse(CepUnavailableException exception) {
        var problem = new Problem();
        problem.type = baseUrl + "/errors/cep-unavailable";
        problem.title = "Consulta de CEP indisponível";
        problem.status = Response.Status.BAD_GATEWAY.getStatusCode();
        problem.detail = "Não foi possível consultar o CEP automaticamente. Preencha o endereço manualmente.";
        problem.instance = uri == null ? null : uri.getRequestUri().getPath();
        problem.timestamp = OffsetDateTime.now();
        return Response.status(problem.status).type("application/problem+json").entity(problem).build();
    }
}
