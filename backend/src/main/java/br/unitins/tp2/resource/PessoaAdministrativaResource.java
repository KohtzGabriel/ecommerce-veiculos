package br.unitins.tp2.resource;

import br.unitins.tp2.dto.PessoaAdministrativaRequest;
import br.unitins.tp2.dto.PessoaAdministrativaResponse;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.mapper.EnderecoMapper;
import br.unitins.tp2.model.*;
import br.unitins.tp2.repository.PessoaRepository;
import br.unitins.tp2.repository.UsuarioRepository;
import br.unitins.tp2.service.ClienteService;
import br.unitins.tp2.service.SenhaService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;

@Path("admin/pessoas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRADOR")
public class PessoaAdministrativaResource {
 @Inject PessoaRepository pessoas;
 @Inject UsuarioRepository usuarios;
 @Inject ClienteService clientes;
 @Inject SenhaService senhas;

 @GET @Transactional public List<PessoaAdministrativaResponse> all(){return pessoas.findAll().list().stream().map(this::response).toList();}
 @GET @Path("/{id}") @Transactional public PessoaAdministrativaResponse get(@PathParam("id") long id){return response(find(id));}
 @POST @Transactional public Response create(@Valid PessoaAdministrativaRequest dto, @Context UriInfo uri){
   Pessoa p=novo(dto); apply(p,dto);
   if (dto.cadastrarFornecedor() && !(p instanceof PessoaJuridica)) throw ValidationException.of("cadastrarFornecedor", "Fornecedor deve ser uma pessoa jurídica.");
   if (dto.usuario() != null && !(p instanceof PessoaFisica)) throw ValidationException.of("usuario", "Usuário deve ser uma pessoa física.");
   if (dto.usuario() != null && Perfil.valueOf(dto.usuario().perfilId()) == null) throw ValidationException.of("usuario.perfilId", "Perfil de acesso inválido.");
   if (pessoas.findByEmailExceptId(dto.email(), null) != null) throw ValidationException.of("email", "Já existe uma pessoa cadastrada com esse e-mail.");
   if (dto.usuario() != null && usuarios.findByLogin(dto.email()) != null) throw ValidationException.of("email", "Já existe um usuário com esse login.");
   pessoas.persistAndFlush(p);
   if (dto.cadastrarCliente()) clientes.createAssociado(p.getId(), true);
   if (dto.cadastrarFornecedor()) {
     Fornecedor fornecedor = new Fornecedor();
     fornecedor.setPessoaJuridica((PessoaJuridica) p);
     pessoas.getEntityManager().persist(fornecedor);
   }
   if (dto.usuario() != null) {
     Usuario usuario = new Usuario();
     usuario.setPessoa(p); usuario.setLogin(p.getEmail());
     usuario.setSenha(senhas.gerarHash(p.getEmail(), dto.usuario().senha()));
     usuario.setPerfis(java.util.Set.of(Perfil.valueOf(dto.usuario().perfilId())));
     usuarios.persist(usuario);
   }
   pessoas.getEntityManager().flush();
   return Response.created(uri.getAbsolutePathBuilder().path(p.getId().toString()).build()).entity(response(p)).build();
 }
 @PUT @Path("/{id}") @Transactional public PessoaAdministrativaResponse update(@PathParam("id") long id,@Valid PessoaAdministrativaRequest dto){
   Pessoa p=find(id);
   String tipo=dto.tipoPessoa().toUpperCase(java.util.Locale.ROOT);
   if(!tipo.equals("FISICA")&&!tipo.equals("JURIDICA")) throw ValidationException.of("tipoPessoa","Use FISICA ou JURIDICA.");
   boolean pf=p instanceof PessoaFisica;
   if(pf != tipo.equals("FISICA")) throw ValidationException.of("tipoPessoa","Não é possível alterar o tipo da pessoa.");
   apply(p,dto); return response(p);
 }
 @DELETE @Path("/{id}") @Transactional public void delete(@PathParam("id") long id){ Pessoa p=find(id); if(pessoas.getEntityManager().createQuery("select count(c) from Cliente c where c.pessoa.id=:id",Long.class).setParameter("id",id).getSingleResult()>0) throw ValidationException.of("pessoa","Pessoa vinculada a cliente não pode ser removida."); pessoas.delete(p); }
 private Pessoa find(long id){Pessoa p=pessoas.findById(id);if(p==null)throw new ResourceNotFoundException("Pessoa não encontrada.");return p;}
 private Pessoa novo(PessoaAdministrativaRequest d){return switch(d.tipoPessoa().toUpperCase()){case "FISICA" -> new PessoaFisica();case "JURIDICA" -> new PessoaJuridica();default -> throw ValidationException.of("tipoPessoa","Use FISICA ou JURIDICA.");};}
 private void apply(Pessoa p,PessoaAdministrativaRequest d){
   p.setNome(d.nome());p.setEmail(d.email());p.setGenero(d.genero());
   if(p instanceof PessoaFisica pf){if(d.cpf()==null||d.dataNascimento()==null)throw ValidationException.of("cpf","CPF e data de nascimento são obrigatórios para pessoa física.");pf.setCpf(d.cpf());pf.setDataNascimento(d.dataNascimento());}
   else if(p instanceof PessoaJuridica pj){if(d.cnpj()==null||d.razaoSocial()==null)throw ValidationException.of("cnpj","CNPJ e razão social são obrigatórios para pessoa jurídica.");pj.setCnpj(d.cnpj());pj.setRazaoSocial(d.razaoSocial());pj.setNomeFantasia(d.nomeFantasia());}
   if(d.endereco()!=null){var e=d.endereco();Endereco a=p.getEndereco();if(a==null)a=new Endereco();a.setCep(e.cep().replaceAll("\\D",""));a.setLogradouro(e.logradouro());a.setNumero(e.numero());a.setComplemento(e.complemento());a.setBairro(e.bairro());a.setCidade(e.cidade());a.setEstado(e.estado().toUpperCase(java.util.Locale.ROOT));p.setEndereco(a);}
 }
 private PessoaAdministrativaResponse response(Pessoa p){PessoaFisica pf=p instanceof PessoaFisica f?f:null;PessoaJuridica pj=p instanceof PessoaJuridica j?j:null;return new PessoaAdministrativaResponse(p.getId(),pf==null?"JURIDICA":"FISICA",p.getNome(),p.getEmail(),pf==null?null:pf.getCpf(),pf==null?null:pf.getDataNascimento(),pj==null?null:pj.getCnpj(),pj==null?null:pj.getRazaoSocial(),pj==null?null:pj.getNomeFantasia(),p.getEndereco()==null?null:EnderecoMapper.toResponse(p.getEndereco()));}
}
