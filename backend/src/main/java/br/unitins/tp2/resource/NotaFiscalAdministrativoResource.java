package br.unitins.tp2.resource;

import br.unitins.tp2.dto.NotaFiscalRequest;
import br.unitins.tp2.dto.NotaFiscalResponse;
import br.unitins.tp2.model.NotaFiscal;
import br.unitins.tp2.service.NotaFiscalService;
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
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("admin/notas-fiscais")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRADOR")
public class NotaFiscalAdministrativoResource {
    @Inject NotaFiscalService notas;

    @GET
    @Transactional
    public java.util.List<NotaFiscalResponse> listar() {
        return notas.listar().stream().map(this::response).toList();
    }

    @POST
    @Transactional
    public Response criar(@Valid NotaFiscalRequest request, @Context UriInfo uri) {
        var nota = notas.criar(request);
        return Response.created(uri.getAbsolutePathBuilder().path(nota.getId().toString()).build())
                .entity(response(nota)).build();
    }

    @GET
    @Path("/{id}")
    @Transactional
    public NotaFiscalResponse buscar(@PathParam("id") long id) {
        return response(notas.buscar(id));
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response excluir(@PathParam("id") long id) {
        notas.excluir(id);
        return Response.noContent().build();
    }

    private NotaFiscalResponse response(NotaFiscal nota) {
        return new NotaFiscalResponse(nota.getId(), nota.getNumero(), nota.getSerie(), nota.getDataEmissao(),
                nota.isEntrada(), nota.getCliente() == null ? null : nota.getCliente().getId(),
                nota.getFornecedor() == null ? null : nota.getFornecedor().getId(), nota.getValorTotal());
    }
}
