package br.unitins.tp2.resource;

import java.util.List;

import br.unitins.tp2.dto.EstadoRequestDTO;
import br.unitins.tp2.dto.EstadoResponseDTO;
import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.mapper.EstadoResponseMapper;
import br.unitins.tp2.model.Estado;
import br.unitins.tp2.service.EstadoService;
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

@Path("estados")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EstadoResource {

    @Inject
    EstadoService service;

    @GET
    public PageResponse<EstadoResponseDTO> buscarTodos(@QueryParam("page") @DefaultValue("0") int page,
                                    @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Estado> estados = service.findAll(page, pageSize);
        long totalItems = service.count();

        return PageResponse.of(estados, page, pageSize, totalItems, EstadoResponseMapper::toResponse);
    }

    @GET
    @Path("/nome/{nome}")
    public PageResponse<EstadoResponseDTO> buscarPorNome(@PathParam("nome") String nome, @QueryParam("page") @DefaultValue("0") int page,
                                    @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Estado> estados = service.findByNome(nome, page, pageSize);
        long totalItems = service.count(nome);

        return PageResponse.of(estados, page, pageSize, totalItems, EstadoResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public EstadoResponseDTO buscarPorId(@PathParam("id") Long id) {
        return EstadoResponseMapper.toResponse(service.findById(id));
    }

    @GET
    @Path("/sigla/{sigla}")
    public EstadoResponseDTO buscarPorSigla(@PathParam("sigla") String sigla) {
        return EstadoResponseMapper.toResponse(service.findBySigla(sigla));
    }

    @POST
    public EstadoResponseDTO incluir(EstadoRequestDTO dto) {
        return EstadoResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, EstadoRequestDTO estado) {
        service.update(id, estado);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }

}
