package br.unitins.tp2.service;

import java.util.ArrayList;
import java.util.List;

import br.unitins.tp2.dto.PacienteRequestDTO;
import br.unitins.tp2.dto.PacienteResponseDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.mapper.PacienteResponseMapper;
import br.unitins.tp2.mapper.TelefoneMapper;
import br.unitins.tp2.model.Paciente;
import br.unitins.tp2.model.Pessoa;
import br.unitins.tp2.model.PessoaFisica;
import br.unitins.tp2.repository.PacienteRepository;
import br.unitins.tp2.repository.PessoaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PacienteServiceImpl implements PacienteService {

    @Inject
    PacienteRepository pacienteRepository;

    @Inject
    PessoaRepository pessoaRepository;

    @Inject
    UsuarioService usuarioService;

    @Override
    @Transactional
    public Paciente create(PacienteRequestDTO paciente) {
        validarDados(paciente, null, null);

        Paciente novoPaciente = new Paciente();
        novoPaciente.setPessoa(new PessoaFisica());
        aplicarDados(novoPaciente, paciente);

        pacienteRepository.persist(novoPaciente);
        return novoPaciente;
    }

    @Override
    @Transactional
    public void update(long id, PacienteRequestDTO paciente) {
        Paciente edicaoPaciente = buscarPacienteOuFalhar(id);
        validarDados(paciente, id, edicaoPaciente.getPessoa().getId());
        aplicarDados(edicaoPaciente, paciente);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Paciente paciente = buscarPacienteOuFalhar(id);
        pacienteRepository.delete(paciente);
    }

    @Override
    public Paciente findById(long id) {
        return buscarPacienteOuFalhar(id);
    }

    @Override
    public PacienteResponseDTO findByCpf(String cpf) {
        Paciente paciente = pacienteRepository.findByCpf(cpf);
        if (paciente != null) {
            return PacienteResponseMapper.toResponse(paciente);
        }

        Pessoa pessoa = pessoaRepository.findByCpf(cpf);
        if (pessoa != null) {
            return PacienteResponseMapper.toResponse(pessoa);
        }

        throw new ResourceNotFoundException("Pessoa não encontrada.");
    }

    @Override
    public List<Paciente> findAll(int page, int pageSize) {
        return pacienteRepository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<Paciente> findByNome(String nome, int page, int pageSize) {
        return pacienteRepository.findByNome(nome).page(page, pageSize).list();
    }

    @Override
    public long count() {
        return pacienteRepository.findAll().count();
    }

    @Override
    public long count(String nome) {
        return pacienteRepository.findByNome(nome).count();
    }

    private void validarDados(PacienteRequestDTO dto, Long idPaciente, Long idPessoa) {
        usuarioService.validarAlteracaoEmail(idPessoa, dto.email());

        if (pessoaRepository.findByCpfExceptId(dto.cpf(), idPessoa) != null) {
            throw ValidationException.of("cpf", "Já existe uma pessoa cadastrada com esse CPF.");
        }

        if (pessoaRepository.findByEmailExceptId(dto.email(), idPessoa) != null) {
            throw ValidationException.of("email", "Já existe uma pessoa cadastrada com esse e-mail.");
        }
    }

    private void aplicarDados(Paciente paciente, PacienteRequestDTO dto) {
        Pessoa pessoa = paciente.getPessoa();
        pessoa.setNome(dto.nome());
        pessoa.setEmail(dto.email());
        pessoa.setCpf(dto.cpf());
        pessoa.setDataNascimento(dto.dataNascimento());
        pessoa.setGenero(dto.genero());
        pessoa.setEnderecoLegado(dto.endereco());
        pessoa.setTelefones(new ArrayList<>(dto.telefones().stream().map(TelefoneMapper::toModel).toList()));

        paciente.setObservacoes(dto.observacoes());
        paciente.setAtivo(dto.ativo());
    }

    private Paciente buscarPacienteOuFalhar(long id) {
        Paciente paciente = pacienteRepository.findById(id);
        if (paciente == null) {
            throw new ResourceNotFoundException("Paciente não encontrado.");
        }

        return paciente;
    }
}
