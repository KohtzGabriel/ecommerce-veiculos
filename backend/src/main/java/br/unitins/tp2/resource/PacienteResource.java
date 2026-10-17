package br.unitins.tp2.resource;

import java.util.List;

import br.unitins.tp2.dto.PacienteRequestDTO;
import br.unitins.tp2.dto.PacienteResponseDTO;
import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.mapper.PacienteResponseMapper;
import br.unitins.tp2.model.Paciente;
import br.unitins.tp2.service.PacienteService;
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

@Path("pacientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PacienteResource {

    @Inject
    PacienteService service;

    @GET
    public PageResponse<PacienteResponseDTO> buscarTodos(@QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Paciente> pacientes = service.findAll(page, pageSize);
        long totalItems = service.count();

        return PageResponse.of(pacientes, page, pageSize, totalItems, PacienteResponseMapper::toResponse);
    }

    @GET
    @Path("/nome/{nome}")
    public PageResponse<PacienteResponseDTO> buscarPorNome(@PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Paciente> pacientes = service.findByNome(nome, page, pageSize);
        long totalItems = service.count(nome);

        return PageResponse.of(pacientes, page, pageSize, totalItems, PacienteResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public PacienteResponseDTO buscarPorId(@PathParam("id") Long id) {
        return PacienteResponseMapper.toResponse(service.findById(id));
    }

    @GET
    @Path("/cpf/{cpf}")
    public PacienteResponseDTO buscarPorCpf(@PathParam("cpf") String cpf) {
        return service.findByCpf(cpf);
    }

    @POST
    public PacienteResponseDTO incluir(PacienteRequestDTO dto) {
        return PacienteResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, PacienteRequestDTO paciente) {
        service.update(id, paciente);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }
}