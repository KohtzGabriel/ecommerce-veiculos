package br.unitins.tp2.resource;

import java.util.List;
import br.unitins.tp2.dto.EnderecoRequestDTO;
import br.unitins.tp2.dto.EnderecoResponseDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.mapper.EnderecoMapper;
import br.unitins.tp2.model.Endereco;
import br.unitins.tp2.repository.EnderecoRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("admin/enderecos") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRADOR")
public class EnderecoAdministrativoResource {
 @Inject EnderecoRepository enderecos;
 @GET @Transactional public List<EnderecoResponseDTO> all(){return enderecos.listAll().stream().map(EnderecoMapper::toResponse).toList();}
 @GET @Path("/{id}") @Transactional public EnderecoResponseDTO get(@PathParam("id") long id){return EnderecoMapper.toResponse(find(id));}
 @POST @Transactional public Response create(@Valid EnderecoRequestDTO dto,@Context UriInfo uri){Endereco e=apply(new Endereco(),dto);enderecos.persistAndFlush(e);return Response.created(uri.getAbsolutePathBuilder().path(e.getId().toString()).build()).entity(EnderecoMapper.toResponse(e)).build();}
 @PUT @Path("/{id}") @Transactional public EnderecoResponseDTO update(@PathParam("id") long id,@Valid EnderecoRequestDTO dto){return EnderecoMapper.toResponse(apply(find(id),dto));}
 @DELETE @Path("/{id}") @Transactional public void delete(@PathParam("id") long id){Endereco e=find(id);long uses=enderecos.getEntityManager().createQuery("select count(p) from Pessoa p where p.endereco.id=:id",Long.class).setParameter("id",id).getSingleResult();if(uses>0)throw ValidationException.of("endereco","Endereço vinculado a pessoa não pode ser removido.");enderecos.delete(e);}
 private Endereco find(long id){Endereco e=enderecos.findById(id);if(e==null)throw new ResourceNotFoundException("Endereço não encontrado.");return e;}
 private Endereco apply(Endereco e,EnderecoRequestDTO d){e.setCep(d.cep().replaceAll("\\D",""));e.setLogradouro(d.logradouro());e.setNumero(d.numero());e.setComplemento(d.complemento());e.setBairro(d.bairro());e.setCidade(d.cidade());e.setEstado(d.estado().toUpperCase(java.util.Locale.ROOT));return e;}
}
