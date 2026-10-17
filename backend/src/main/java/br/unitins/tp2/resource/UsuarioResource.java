package br.unitins.tp2.resource;

import br.unitins.tp2.dto.UsuarioRequestDTO;
import br.unitins.tp2.dto.UsuarioResponseDTO;
import br.unitins.tp2.dto.UsuarioUpdateDTO;
import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.mapper.UsuarioResponseMapper;
import br.unitins.tp2.service.UsuarioService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRADOR")
public class UsuarioResource {

    @Inject
    UsuarioService service;

    @GET
    public PageResponse<UsuarioResponseDTO> buscarTodos(@QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return service.findAll(page, pageSize);
    }

    @GET
    @Path("/nome/{nome}")
    public PageResponse<UsuarioResponseDTO> buscarPorNome(@PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return service.findByNome(nome, page, pageSize);
    }

    @GET
    @Path("/cpf/{cpf}")
    public UsuarioResponseDTO buscarPorCpf(@PathParam("cpf") String cpf) {
        return service.findByCpf(cpf);
    }

    @GET
    @Path("/{id}")
    public UsuarioResponseDTO buscarPorId(@PathParam("id") long id) {
        return service.findById(id);
    }

    @PUT
    @Path("/{id}")
    public UsuarioResponseDTO alterar(@PathParam("id") long id, @NotNull @Valid UsuarioUpdateDTO dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response apagar(@PathParam("id") long id) {
        service.delete(id);
        return Response.noContent().build();
    }

    @POST
    public Response incluir(@NotNull @Valid UsuarioRequestDTO dto) {
        return Response.status(Response.Status.CREATED)
                .entity(UsuarioResponseMapper.toResponse(service.create(dto)))
                .build();
    }
}
