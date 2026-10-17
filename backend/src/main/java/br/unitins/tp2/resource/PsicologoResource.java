package br.unitins.tp2.resource;

import java.util.List;

import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.dto.PsicologoRequestDTO;
import br.unitins.tp2.dto.PsicologoResponseDTO;
import br.unitins.tp2.mapper.PsicologoResponseMapper;
import br.unitins.tp2.model.Psicologo;
import br.unitins.tp2.service.PsicologoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("psicologos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PsicologoResource {

    @Inject
    PsicologoService service;

    @GET
    public PageResponse<PsicologoResponseDTO> buscarTodos(@QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Psicologo> psicologos = service.findAll(page, pageSize);
        long totalItems = service.count();

        return PageResponse.of(psicologos, page, pageSize, totalItems, PsicologoResponseMapper::toResponse);
    }

    @GET
    @Path("/nome/{nome}")
    public PageResponse<PsicologoResponseDTO> buscarPorNome(@PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Psicologo> psicologos = service.findByNome(nome, page, pageSize);
        long totalItems = service.count(nome);

        return PageResponse.of(psicologos, page, pageSize, totalItems, PsicologoResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public PsicologoResponseDTO buscarPorId(@PathParam("id") Long id) {
        return PsicologoResponseMapper.toResponse(service.findById(id));
    }

    @GET
    @Path("/cpf/{cpf}")
    public PsicologoResponseDTO buscarPorCpf(@PathParam("cpf") String cpf) {
        return service.findByCpf(cpf);
    }

    @GET
    @Path("/crp/{crp}")
    public PsicologoResponseDTO buscarPorCrp(@PathParam("crp") String crp) {
        return PsicologoResponseMapper.toResponse(service.findByCrp(crp));
    }

    @POST
    public PsicologoResponseDTO incluir(PsicologoRequestDTO dto) {
        return PsicologoResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, PsicologoRequestDTO psicologo) {
        service.update(id, psicologo);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }
}