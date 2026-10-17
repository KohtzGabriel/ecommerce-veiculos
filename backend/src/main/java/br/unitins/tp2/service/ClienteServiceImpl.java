package br.unitins.tp2.service;

import java.util.ArrayList;

import br.unitins.tp2.dto.ClienteRequestDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ResourceConflictException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.mapper.TelefoneMapper;
import br.unitins.tp2.model.Cliente;
import br.unitins.tp2.model.Endereco;
import br.unitins.tp2.model.Pessoa;
import br.unitins.tp2.model.PessoaFisica;
import br.unitins.tp2.repository.ClienteRepository;
import br.unitins.tp2.repository.PessoaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ClienteServiceImpl implements ClienteService {

    @Inject
    ClienteRepository clienteRepository;

    @Inject
    PessoaRepository pessoaRepository;

    @Override
    @Transactional
    public Cliente create(ClienteRequestDTO dto) {
        validarDuplicidade(dto);

        var pessoa = new PessoaFisica();
        pessoa.setNome(dto.nome());
        pessoa.setEmail(dto.email());
        pessoa.setCpf(dto.cpf());
        pessoa.setDataNascimento(dto.dataNascimento());
        pessoa.setGenero(dto.genero());
        pessoa.setTelefones(new ArrayList<>(dto.telefones().stream().map(TelefoneMapper::toModel).toList()));

        var endereco = new Endereco();
        endereco.setCep(CepNormalizer.normalize(dto.endereco().cep()));
        endereco.setLogradouro(dto.endereco().logradouro());
        endereco.setNumero(dto.endereco().numero());
        endereco.setComplemento(dto.endereco().complemento());
        endereco.setBairro(dto.endereco().bairro());
        endereco.setCidade(dto.endereco().cidade());
        endereco.setEstado(dto.endereco().estado().toUpperCase(java.util.Locale.ROOT));

        var cliente = new Cliente();
        cliente.setPessoa(pessoa);
        pessoa.setEndereco(endereco);
        cliente.setAtivo(true);
        pessoaRepository.persistAndFlush(pessoa);
        clienteRepository.persist(cliente);
        return cliente;
    }

    @Override
    @Transactional
    public Cliente createAssociado(long pessoaId, boolean ativo) {
        Pessoa pessoa = pessoaRepository.findById(pessoaId);
        if (pessoa == null) {
            throw new ResourceNotFoundException("Pessoa não encontrada.");
        }
        if (clienteRepository.find("pessoa.id", pessoaId).firstResult() != null) {
            throw new ResourceConflictException("A pessoa informada já está cadastrada como cliente.");
        }
        Cliente cliente = new Cliente();
        cliente.setPessoa(pessoa);
        cliente.setAtivo(ativo);
        clienteRepository.persist(cliente);
        return cliente;
    }

    @Override
    @Transactional
    public void delete(long id) {
        Cliente cliente = findById(id);
        if (clienteRepository.hasVenda(id)) {
            throw new ResourceConflictException("Cliente com compra registrada em nota fiscal não pode ser excluído.");
        }
        clienteRepository.delete(cliente);
    }

    @Override
    public Cliente findById(long id) {
        var cliente = clienteRepository.findById(id);
        if (cliente == null) {
            throw new ResourceNotFoundException("Cliente não encontrado.");
        }
        return cliente;
    }

    @Override
    public Cliente findByCpf(String cpf) {
        String normalizedCpf = cpf == null ? "" : cpf.replaceAll("\\D", "");
        var cliente = clienteRepository.findByCpf(normalizedCpf);
        if (cliente == null) throw new ResourceNotFoundException("Cliente não encontrado.");
        return cliente;
    }

    private void validarDuplicidade(ClienteRequestDTO dto) {
        if (pessoaRepository.findByCpf(dto.cpf()) != null) {
            throw ValidationException.of("cpf", "Já existe uma pessoa cadastrada com esse CPF.");
        }
        if (pessoaRepository.findByEmailExceptId(dto.email(), null) != null) {
            throw ValidationException.of("email", "Já existe uma pessoa cadastrada com esse e-mail.");
        }
    }
}
