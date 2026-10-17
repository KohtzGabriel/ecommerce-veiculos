package br.unitins.tp2.client;

import br.unitins.tp2.dto.ViaCepResponseDTO;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/{cep}/json")
@RegisterRestClient(configKey = "viacep-api")
public interface ViaCepClient {

    @GET
    ViaCepResponseDTO buscar(@PathParam("cep") String cep);
}
