package br.unitins.tp2.resource;
import br.unitins.tp2.model.*;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ResourceConflictException;
import br.unitins.tp2.repository.FornecedorRepository;
import br.unitins.tp2.repository.PessoaRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;
@Path("admin/fornecedores") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON) @RolesAllowed("ADMINISTRADOR")
public class FornecedorAdministrativoResource {
 public record Input(@NotNull Long pessoaJuridicaId, boolean ativo){} public record Output(Long id,Long pessoaJuridicaId,String cnpj,String razaoSocial,boolean ativo){}
 @Inject FornecedorRepository fornecedores; @Inject PessoaRepository pessoas;
 @GET @Transactional public List<Output> all(){return fornecedores.listAll().stream().map(this::out).toList();}
 @GET @Path("/{id}") @Transactional public Output get(@PathParam("id")long id){return out(find(id));}
 @POST @Transactional public Response create(@jakarta.validation.Valid Input dto,@Context UriInfo uri){var f=new Fornecedor();f.setPessoaJuridica(pj(dto.pessoaJuridicaId()));f.setAtivo(dto.ativo());fornecedores.persistAndFlush(f);return Response.created(uri.getAbsolutePathBuilder().path(f.getId().toString()).build()).entity(out(f)).build();}
 @PUT @Path("/{id}") @Transactional public Output update(@PathParam("id")long id,@jakarta.validation.Valid Input dto){var f=find(id);f.setPessoaJuridica(pj(dto.pessoaJuridicaId()));f.setAtivo(dto.ativo());return out(f);}
 @DELETE @Path("/{id}") @Transactional public void delete(@PathParam("id")long id){var f=find(id);if(fornecedores.hasNotaFiscal(id))throw new ResourceConflictException("Fornecedor associado a nota fiscal não pode ser removido.");fornecedores.delete(f);}
 private Fornecedor find(long id){var f=fornecedores.findById(id);if(f==null)throw new ResourceNotFoundException("Fornecedor não encontrado.");return f;}
 private PessoaJuridica pj(long id){var p=pessoas.findById(id);if(!(p instanceof PessoaJuridica j))throw new ResourceNotFoundException("Pessoa Jurídica não encontrada.");return j;}
 private Output out(Fornecedor f){var p=f.getPessoaJuridica();return new Output(f.getId(),p.getId(),p.getCnpj(),p.getRazaoSocial(),f.isAtivo());}
}
