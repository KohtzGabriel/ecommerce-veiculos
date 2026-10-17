package br.unitins.tp2.resource;

import java.util.List;

import br.unitins.tp2.dto.VeiculoResponse;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.StatusVeiculo;
import br.unitins.tp2.model.Veiculo;
import br.unitins.tp2.service.VeiculoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.Produces;

@Path("admin/veiculos")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRADOR")
public class VeiculoAdministrativoResource {
    @Inject VeiculoService veiculos;

    @GET
    @Transactional
    public List<VeiculoResponse> listar(@QueryParam("anoModelo") Integer anoModelo,
            @QueryParam("modelo") String modelo, @QueryParam("marcaId") Long marcaId,
            @QueryParam("status") String status) {
        if (anoModelo != null && (anoModelo < 1886 || anoModelo > 2200))
            throw ValidationException.of("anoModelo", "Ano do modelo inválido.");
        StatusVeiculo estado = parseStatus(status);
        return veiculos.buscar(anoModelo, modelo, marcaId, estado).stream().map(this::response).toList();
    }

    @GET
    @Path("/{id}")
    @Transactional
    public VeiculoResponse buscar(@PathParam("id") long id) {
        return response(veiculos.buscar(id));
    }

    private StatusVeiculo parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try { return StatusVeiculo.valueOf(status.strip().toUpperCase(java.util.Locale.ROOT)); }
        catch (IllegalArgumentException e) { throw ValidationException.of("status", "Status de veículo inválido."); }
    }

    private VeiculoResponse response(Veiculo v) {
        return new VeiculoResponse(v.getId(), v.getDescricao(), v.getValorCusto(), v.getValorVenda(),
                v.isAtivo(), v.getModelo(), v.getAnoFabricacao(), v.getAnoModelo(), v.getCor(),
                v.getPlaca(), v.getChassi(), v.getRenavam(), v.getQuilometragem(),
                v.getMarca().getId(), v.getMarca().getNome(), v.getStatus());
    }
}
