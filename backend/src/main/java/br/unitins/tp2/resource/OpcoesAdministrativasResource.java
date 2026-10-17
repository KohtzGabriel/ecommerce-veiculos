package br.unitins.tp2.resource;
import br.unitins.tp2.model.Perfil;
import br.unitins.tp2.model.StatusVeiculo;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.Arrays;
import java.util.List;
@Path("admin") @RolesAllowed("ADMINISTRADOR")
public class OpcoesAdministrativasResource {
 @GET @Path("perfis") public List<Perfil> perfis(){return Arrays.asList(Perfil.values());}
 @GET @Path("status-veiculos") public List<StatusVeiculo> status(){return Arrays.asList(StatusVeiculo.values());}
}
