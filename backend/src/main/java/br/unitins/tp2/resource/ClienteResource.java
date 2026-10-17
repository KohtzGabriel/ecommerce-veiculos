package br.unitins.tp2.resource;

import br.unitins.tp2.dto.ClienteRequestDTO;
import br.unitins.tp2.dto.ClienteResponseDTO;
import br.unitins.tp2.mapper.ClienteResponseMapper;
import br.unitins.tp2.service.ClienteService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteResource {

    @Inject
    ClienteService service;

    @POST
    public ClienteResponseDTO incluir(@Valid ClienteRequestDTO dto) {
        return ClienteResponseMapper.toResponse(service.create(dto));
    }

    @GET
    @Path("/{id}")
    public ClienteResponseDTO buscarPorId(@PathParam("id") long id) {
        return ClienteResponseMapper.toResponse(service.findById(id));
    }
}
