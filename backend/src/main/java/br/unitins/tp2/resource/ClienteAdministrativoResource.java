package br.unitins.tp2.resource;

import br.unitins.tp2.dto.ClienteAssociacaoRequest;
import br.unitins.tp2.dto.ClienteAssociacaoResponse;
import br.unitins.tp2.model.Cliente;
import br.unitins.tp2.model.PessoaFisica;
import br.unitins.tp2.service.ClienteService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("admin/clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRADOR")
public class ClienteAdministrativoResource {
    @Inject ClienteService clientes;

    @GET
    @Path("/{id}")
    @Transactional
    public ClienteAssociacaoResponse get(@PathParam("id") long id) {
        return response(clientes.findById(id));
    }

    @GET
    @Path("/cpf/{cpf}")
    @Transactional
    public ClienteAssociacaoResponse getByCpf(@PathParam("cpf") String cpf) {
        return response(clientes.findByCpf(cpf));
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public void delete(@PathParam("id") long id) {
        clientes.delete(id);
    }

    @POST
    @Transactional
    public Response create(@Valid ClienteAssociacaoRequest request, @Context UriInfo uri) {
        Cliente cliente = clientes.createAssociado(request.pessoaId(), request.ativo());
        return Response.created(uri.getAbsolutePathBuilder().path(cliente.getId().toString()).build())
                .entity(response(cliente)).build();
    }

    private ClienteAssociacaoResponse response(Cliente cliente) {
        String tipoPessoa = cliente.getPessoa() instanceof PessoaFisica ? "FISICA" : "JURIDICA";
        return new ClienteAssociacaoResponse(cliente.getId(), cliente.getPessoa().getId(),
                tipoPessoa, cliente.isAtivo());
    }
}
