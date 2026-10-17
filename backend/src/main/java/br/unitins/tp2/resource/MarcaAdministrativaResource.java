package br.unitins.tp2.resource;
import br.unitins.tp2.model.Marca;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;
@Path("admin/marcas") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON) @RolesAllowed("ADMINISTRADOR")
public class MarcaAdministrativaResource {
 public record MarcaInput(@NotBlank String nome){} public record MarcaOutput(Long id,String nome){}
 @Inject MarcaRepository marcas;
 @GET @Transactional public List<MarcaOutput> all(){return marcas.listAll().stream().map(this::out).toList();}
 @GET @Path("/{id}") @Transactional public MarcaOutput get(@PathParam("id") long id){return out(find(id));}
 @POST @Transactional public Response create(@jakarta.validation.Valid MarcaInput dto,@Context UriInfo uri){Marca m=new Marca();m.setNome(dto.nome());marcas.persistAndFlush(m);return Response.created(uri.getAbsolutePathBuilder().path(m.getId().toString()).build()).entity(out(m)).build();}
 @PUT @Path("/{id}") @Transactional public MarcaOutput update(@PathParam("id") long id,@jakarta.validation.Valid MarcaInput dto){Marca m=find(id);m.setNome(dto.nome());return out(m);}
 @DELETE @Path("/{id}") @Transactional public void delete(@PathParam("id") long id){Marca m=find(id);if(marcas.getEntityManager().createQuery("select count(v) from Veiculo v where v.marca.id=:id",Long.class).setParameter("id",id).getSingleResult()>0)throw ValidationException.of("marca","Marca vinculada a veículo não pode ser removida.");marcas.delete(m);}
 private Marca find(long id){Marca m=marcas.findById(id);if(m==null)throw new ResourceNotFoundException("Marca não encontrada.");return m;}
 private MarcaOutput out(Marca m){return new MarcaOutput(m.getId(),m.getNome());}
 @ApplicationScoped public static class MarcaRepository implements PanacheRepository<Marca>{}
}
