package br.unitins.tp2.service;

import java.util.ArrayList;
import java.util.List;

import br.unitins.tp2.dto.PsicologoRequestDTO;
import br.unitins.tp2.dto.PsicologoResponseDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.mapper.PsicologoResponseMapper;
import br.unitins.tp2.mapper.TelefoneMapper;
import br.unitins.tp2.model.Pessoa;
import br.unitins.tp2.model.PessoaFisica;
import br.unitins.tp2.model.Psicologo;
import br.unitins.tp2.repository.PessoaRepository;
import br.unitins.tp2.repository.PsicologoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PsicologoServiceImpl implements PsicologoService {

    @Inject
    PsicologoRepository psicologoRepository;

    @Inject
    PessoaRepository pessoaRepository;

    @Inject
    UsuarioService usuarioService;

    @Override
    @Transactional
    public Psicologo create(PsicologoRequestDTO psicologo) {
        Pessoa pessoaExistente = pessoaRepository.findByCpf(psicologo.cpf());

        if (psicologoRepository.findByCpf(psicologo.cpf()) != null) {
            throw ValidationException.of("cpf", "Já existe um psicólogo cadastrado com esse CPF.");
        }

        validarDados(psicologo, null, pessoaExistente != null ? pessoaExistente.getId() : null);

        Psicologo novoPsicologo = new Psicologo();
        novoPsicologo.setPessoa(pessoaExistente != null ? pessoaExistente : new PessoaFisica());
        aplicarDados(novoPsicologo, psicologo);

        psicologoRepository.persist(novoPsicologo);
        return novoPsicologo;
    }

    @Override
    @Transactional
    public void update(long id, PsicologoRequestDTO psicologo) {
        Psicologo edicaoPsicologo = buscarPsicologoOuFalhar(id);
        validarDados(psicologo, id, edicaoPsicologo.getPessoa().getId());
        aplicarDados(edicaoPsicologo, psicologo);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Psicologo psicologo = buscarPsicologoOuFalhar(id);
        psicologoRepository.delete(psicologo);
    }

    @Override
    public Psicologo findById(long id) {
        return buscarPsicologoOuFalhar(id);
    }

    @Override
    public PsicologoResponseDTO findByCpf(String cpf) {
        Psicologo psicologo = psicologoRepository.findByCpf(cpf);
        if (psicologo != null) {
            return PsicologoResponseMapper.toResponse(psicologo);
        }

        Pessoa pessoa = pessoaRepository.findByCpf(cpf);
        if (pessoa != null) {
            return PsicologoResponseMapper.toResponse(pessoa);
        }

        throw new ResourceNotFoundException("Pessoa não encontrada.");
    }

    @Override
    public Psicologo findByCrp(String crp) {
        Psicologo psicologo = psicologoRepository.findByCrp(crp);
        if (psicologo == null) {
            throw new ResourceNotFoundException("Psicólogo não encontrado.");
        }

        return psicologo;
    }

    @Override
    public List<Psicologo> findAll(int page, int pageSize) {
        return psicologoRepository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<Psicologo> findByNome(String nome, int page, int pageSize) {
        return psicologoRepository.findByNome(nome).page(page, pageSize).list();
    }

    @Override
    public long count() {
        return psicologoRepository.findAll().count();
    }

    @Override
    public long count(String nome) {
        return psicologoRepository.findByNome(nome).count();
    }

    private void validarDados(PsicologoRequestDTO dto, Long idPsicologo, Long idPessoa) {
        usuarioService.validarAlteracaoEmail(idPessoa, dto.email());

        if (pessoaRepository.findByCpfExceptId(dto.cpf(), idPessoa) != null) {
            throw ValidationException.of("cpf", "Já existe uma pessoa cadastrada com esse CPF.");
        }

        if (pessoaRepository.findByEmailExceptId(dto.email(), idPessoa) != null) {
            throw ValidationException.of("email", "Já existe uma pessoa cadastrada com esse e-mail.");
        }

        if (psicologoRepository.findByCrpExceptId(dto.crp(), idPsicologo) != null) {
            throw ValidationException.of("crp", "Já existe um psicólogo cadastrado com esse CRP.");
        }
    }

    private void aplicarDados(Psicologo psicologo, PsicologoRequestDTO dto) {
        Pessoa pessoa = psicologo.getPessoa();
        pessoa.setNome(dto.nome());
        pessoa.setEmail(dto.email());
        pessoa.setCpf(dto.cpf());
        pessoa.setDataNascimento(dto.dataNascimento());
        pessoa.setGenero(dto.genero());
        pessoa.setEnderecoLegado(dto.endereco());
        pessoa.setTelefones(new ArrayList<>(dto.telefones().stream().map(TelefoneMapper::toModel).toList()));

        psicologo.setCrp(dto.crp());
        psicologo.setEspecialidade(dto.especialidade());
        psicologo.setBio(dto.bio());
        psicologo.setValorConsulta(dto.valorConsulta());
        psicologo.setDuracaoConsulta(dto.duracaoConsulta());
        psicologo.setAtivo(dto.ativo());
    }

    private Psicologo buscarPsicologoOuFalhar(long id) {
        Psicologo psicologo = psicologoRepository.findById(id);
        if (psicologo == null) {
            throw new ResourceNotFoundException("Psicólogo não encontrado.");
        }

        return psicologo;
    }
}
