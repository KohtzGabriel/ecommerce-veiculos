package br.unitins.tp2.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import br.unitins.tp2.dto.UsuarioRequestDTO;
import br.unitins.tp2.dto.UsuarioUpdateDTO;
import br.unitins.tp2.dto.UsuarioResponseDTO;
import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.mapper.TelefoneMapper;
import br.unitins.tp2.mapper.UsuarioResponseMapper;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.Perfil;
import br.unitins.tp2.model.Pessoa;
import br.unitins.tp2.model.PessoaFisica;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.repository.PessoaRepository;
import br.unitins.tp2.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UsuarioServiceImpl implements UsuarioService {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    PessoaRepository pessoaRepository;

    @Inject
    SenhaService senhaService;

    @Override
    @Transactional
    public PageResponse<UsuarioResponseDTO> findAll(int page, int pageSize) {
        return listar(null, page, pageSize);
    }

    @Override
    @Transactional
    public PageResponse<UsuarioResponseDTO> findByNome(String nome, int page, int pageSize) {
        return listar(nome, page, pageSize);
    }

    private PageResponse<UsuarioResponseDTO> listar(String nome, int page, int pageSize) {
        if (page < 0 || pageSize < 1 || pageSize > 100 || (long) page * pageSize > Integer.MAX_VALUE) {
            throw ValidationException.of("page", "Use página não negativa e tamanho entre 1 e 100, dentro do limite de paginação.");
        }
        return PageResponse.of(usuarioRepository.listarUsuarios(nome, page, pageSize),
                page, pageSize, usuarioRepository.contarUsuarios(nome),
                UsuarioResponseMapper::toResponse);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO findById(long id) {
        return UsuarioResponseMapper.toResponse(buscarUsuarioOuFalhar(id));
    }

    @Override
    @Transactional
    public UsuarioResponseDTO findByCpf(String cpf) {
        Usuario usuario = usuarioRepository.findByCpf(cpf);
        if (usuario == null) {
            throw new ResourceNotFoundException("Usuário não encontrado.");
        }
        return UsuarioResponseMapper.toResponse(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO update(long id, UsuarioUpdateDTO dto) {
        Usuario usuario = buscarUsuarioOuFalhar(id);
        Set<Perfil> perfis = validarPerfis(dto.perfis());
        if (dto.senha() != null) {
            if (dto.senha().isBlank()) {
                throw ValidationException.of("senha", "A senha não pode conter apenas espaços.");
            }
            usuario.setSenha(senhaService.gerarHash(usuario.getPessoa().getEmail(), dto.senha()));
        }
        usuario.setAtivo(dto.ativo());
        usuario.setPerfis(perfis);
        return UsuarioResponseMapper.toResponse(usuario);
    }

    @Override
    @Transactional
    public void delete(long id) {
        usuarioRepository.delete(buscarUsuarioOuFalhar(id));
    }

    private Usuario buscarUsuarioOuFalhar(long id) {
        Usuario usuario = usuarioRepository.findById(id);
        if (usuario == null) {
            throw new ResourceNotFoundException("Usuário não encontrado.");
        }
        return usuario;
    }

    @Override
    public void validarAlteracaoEmail(Long pessoaId, String email) {
        if (pessoaId == null) {
            return;
        }
        Usuario usuario = usuarioRepository.findByPessoaId(pessoaId);
        if (usuario != null && !usuario.getLogin().equals(email)) {
            throw ValidationException.of("email",
                    "O e-mail está vinculado ao login. A alteração exige um fluxo de troca de credenciais.");
        }
    }

    @Override
    @Transactional
    public Usuario create(UsuarioRequestDTO dto) {
        Pessoa pessoa = pessoaRepository.findByCpf(dto.cpf());
        Long pessoaId = pessoa == null ? null : pessoa.getId();

        if (pessoa != null && usuarioRepository.findByPessoaId(pessoa.getId()) != null) {
            throw ValidationException.of("cpf", "Essa pessoa já possui um usuário.");
        }

        if (pessoaRepository.findByEmailExceptId(dto.email(), pessoaId) != null) {
            throw ValidationException.of("email", "Já existe uma pessoa cadastrada com esse e-mail.");
        }

        if (pessoa == null) {
            pessoa = new PessoaFisica();
        }

        aplicarDados(pessoa, dto);

        String email = pessoa.getEmail();
        if (email == null || email.isBlank() || email.length() > 120) {
            throw ValidationException.of("email", "A pessoa deve possuir um e-mail válido de até 120 caracteres.");
        }
        if (usuarioRepository.findByLogin(email) != null) {
            throw ValidationException.of("email", "Já existe um usuário com o e-mail dessa pessoa.");
        }

        Set<Perfil> perfis = validarPerfis(dto.perfis());

        Usuario usuario = new Usuario();
        usuario.setPessoa(pessoa);
        usuario.setLogin(email);
        usuario.setSenha(senhaService.gerarHash(email, dto.senha()));
        usuario.setPerfis(perfis);
        usuario.setAtivo(true);
        usuarioRepository.persistAndFlush(usuario);
        return usuario;
    }

    private void aplicarDados(Pessoa pessoa, UsuarioRequestDTO dto) {
        pessoa.setNome(dto.nome());
        pessoa.setEmail(dto.email());
        pessoa.setCpf(dto.cpf());
        pessoa.setDataNascimento(dto.dataNascimento());
        pessoa.setGenero(dto.genero());
        pessoa.setEnderecoLegado(dto.endereco());
        pessoa.setTelefones(new ArrayList<>(dto.telefones().stream().map(TelefoneMapper::toModel).toList()));
    }

    private Set<Perfil> validarPerfis(Set<Long> ids) {
        Set<Perfil> perfis = new HashSet<>();
        for (Long id : ids) {
            Perfil perfil = Perfil.valueOf(id);
            if (perfil == null) {
                throw ValidationException.of("perfis", "Perfil inválido: " + id + ".");
            }
            perfis.add(perfil);
        }
        return perfis;
    }
}
