package br.unitins.tp2.resource;

import java.util.List;

import br.unitins.tp2.dto.CatalogoVeiculoResponse;
import br.unitins.tp2.model.Veiculo;
import br.unitins.tp2.service.VeiculoService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("catalogo/veiculos")
@Produces(MediaType.APPLICATION_JSON)
public class CatalogoResource {
    @Inject VeiculoService veiculos;

    @GET
    @Transactional
    public List<CatalogoVeiculoResponse> listar(@QueryParam("anoModelo") Integer anoModelo,
            @QueryParam("modelo") String modelo, @QueryParam("marcaId") Long marcaId) {
        return veiculos.listarDisponiveis(anoModelo, modelo, marcaId).stream().map(this::response).toList();
    }

    private CatalogoVeiculoResponse response(Veiculo v) {
        return new CatalogoVeiculoResponse(v.getId(), v.getDescricao(), v.getValorVenda(), v.getMarca().getId(),
                v.getModelo(), v.getAnoFabricacao(), v.getAnoModelo(), v.getCor(),
                v.getMarca().getNome(), v.getStatus().name());
    }
}
