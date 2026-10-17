package br.unitins.tp2.resource;

import java.util.List;

import br.unitins.tp2.dto.MunicipioRequestDTO;
import br.unitins.tp2.dto.MunicipioResponseDTO;
import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.mapper.MunicipioResponseMapper;
import br.unitins.tp2.model.Municipio;
import br.unitins.tp2.service.MunicipioService;
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

@Path("municipios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MunicipioResource {

    @Inject
    MunicipioService service;

    @GET
    public PageResponse<MunicipioResponseDTO> buscarTodos(@QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Municipio> municipios = service.findAll(page, pageSize);
        long totalItems = service.count();

        return PageResponse.of(municipios, page, pageSize, totalItems, MunicipioResponseMapper::toResponse);
    }

    @GET
    @Path("/nome/{nome}")
    public PageResponse<MunicipioResponseDTO> buscarPorNome(@PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Municipio> municipios = service.findByNome(nome, page, pageSize);
        long totalItems = service.count(nome);

        return PageResponse.of(municipios, page, pageSize, totalItems, MunicipioResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public MunicipioResponseDTO buscarPorId(@PathParam("id") Long id) {
        return MunicipioResponseMapper.toResponse(service.findById(id));
    }

    @POST
    public MunicipioResponseDTO incluir(MunicipioRequestDTO dto) {
        return MunicipioResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, MunicipioRequestDTO municipio) {
        service.update(id, municipio);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }
}