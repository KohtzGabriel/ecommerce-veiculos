package br.unitins.tp2.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.unitins.tp2.dto.TelefoneRequestDTO;
import br.unitins.tp2.dto.UsuarioRequestDTO;
import br.unitins.tp2.dto.UsuarioUpdateDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.mapper.UsuarioResponseMapper;
import br.unitins.tp2.model.Perfil;
import br.unitins.tp2.model.Pessoa;
import br.unitins.tp2.model.PessoaFisica;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.repository.PessoaRepository;
import br.unitins.tp2.repository.UsuarioRepository;

class UsuarioServiceTest {

    private final UsuarioServiceImpl service = new UsuarioServiceImpl();
    private final UsuariosEmMemoria usuarios = new UsuariosEmMemoria();
    private final PessoasEmMemoria pessoas = new PessoasEmMemoria();

    @BeforeEach
    void preparar() {
        usuarios.porPessoa = null;
        usuarios.porCpf = null;
        usuarios.porLogin = null;
        usuarios.persistido = null;
        usuarios.porId = null;
        usuarios.excluido = null;
        usuarios.linhas = List.of();
        usuarios.total = 0;
        usuarios.filtroLista = null;
        usuarios.filtroContagem = null;
        usuarios.page = 0;
        usuarios.pageSize = 0;
        pessoas.pessoa = new PessoaFisica() {
            @Override
            public Long getId() {
                return 10L;
            }
        };
        pessoas.pessoa.setEmail("ana@example.com");
        pessoas.pessoa.setNome("Ana");
        pessoas.pessoa.setCpf("12345678901");
        pessoas.pessoa.setDataNascimento(LocalDate.of(1990, 1, 1));
        pessoas.pessoa.setGenero("F");
        pessoas.pessoa.setEnderecoLegado("Rua A");
        service.pessoaRepository = pessoas;
        service.usuarioRepository = usuarios;
        service.senhaService = new SenhaService();
    }

    @Test
    void deveCadastrarComEmailDaPessoaHashEPerfisSemExporSenha() throws Exception {
        var dto = dto("Ana Maria", "ana.nova@example.com", "12345678901", Set.of(2L));
        Usuario usuario = service.create(dto);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        assertSame(pessoas.pessoa, usuario.getPessoa());
        assertEquals("Ana Maria", pessoas.pessoa.getNome());
        assertEquals("ana.nova@example.com", pessoas.pessoa.getEmail());
        assertEquals(pessoas.pessoa.getEmail(), usuario.getLogin());
        assertEquals(Set.of(Perfil.CLIENTE), usuario.getPerfis());
        assertTrue(usuario.isAtivo());
        assertSame(usuario, usuarios.persistido);
        assertTrue(service.senhaService.verificar(usuario.getLogin(), dto.senha(), usuario.getSenha()));
        assertEquals(1, pessoas.pessoa.getTelefones().size());
        assertEquals("63", pessoas.pessoa.getTelefones().get(0).getCodigoArea());
        assertFalse(dto.toString().contains(dto.senha()));

        var json = objectMapper.writeValueAsString(UsuarioResponseMapper.toResponse(usuario));
        assertFalse(json.contains("senha"));
        assertFalse(json.contains(usuario.getSenha()));
        assertFalse(objectMapper.writeValueAsString(dto).contains(dto.senha()));
    }

    @Test
    void deveCriarPessoaQuandoCpfNaoExiste() {
        pessoas.pessoa = null;
        var usuario = service.create(dto(Set.of(2L)));
        assertNotNull(usuario.getPessoa());
        assertEquals("12345678901", usuario.getPessoa().getCpf());
        assertEquals("ana@example.com", usuario.getLogin());
    }

    @Test
    void deveRejeitarSegundaContaParaMesmaPessoa() {
        usuarios.porPessoa = new Usuario();
        assertThrows(ValidationException.class, () -> service.create(dto(Set.of(2L))));
        assertNull(usuarios.persistido);
    }

    @Test
    void deveRejeitarLoginDuplicado() {
        usuarios.porLogin = new Usuario();
        assertThrows(ValidationException.class, () -> service.create(dto(Set.of(2L))));
        assertNull(usuarios.persistido);
    }

    @Test
    void deveRejeitarPerfilInexistente() {
        assertThrows(ValidationException.class, () -> service.create(dto(Set.of(99L))));
        assertNull(usuarios.persistido);
    }

    @Test
    void devePreservarEmailVinculadoAsCredenciais() {
        usuarios.porPessoa = new Usuario();
        usuarios.porPessoa.setLogin("ana@example.com");
        assertDoesNotThrow(() -> service.validarAlteracaoEmail(10L, "ana@example.com"));
        assertThrows(ValidationException.class,
                () -> service.validarAlteracaoEmail(10L, "outro@example.com"));
        usuarios.porPessoa = null;
        assertDoesNotThrow(() -> service.validarAlteracaoEmail(10L, "outro@example.com"));
    }

    private UsuarioRequestDTO dto(Set<Long> perfis) {
        return dto("Ana", "ana@example.com", "12345678901", perfis);
    }

    private UsuarioRequestDTO dto(String nome, String email, String cpf, Set<Long> perfis) {
        return new UsuarioRequestDTO(
                nome,
                email,
                List.of(new TelefoneRequestDTO("63", "999999999", true)),
                cpf,
                LocalDate.of(1990, 1, 1),
                "F",
                "Rua A",
                "Uma senha longa de teste!",
                perfis);
    }

    @Test
    void deveListarApenasUsuarios() {
        Usuario usuario = service.create(dto(Set.of(2L)));
        usuarios.linhas = List.of(usuario);
        usuarios.total = 1;

        var pagina = service.findAll(0, 10);
        assertEquals(1, pagina.totalItems());
        assertEquals(1, pagina.totalPages());
        assertEquals(1, pagina.items().size());
        var candidato = pagina.items().get(0);
        assertEquals("12345678901", candidato.cpf());
        assertEquals("Ana", candidato.nome());
        assertEquals("ana@example.com", candidato.email());
        assertEquals(usuario.getLogin(), candidato.login());
        assertTrue(candidato.ativo());
        assertEquals(Set.of(Perfil.CLIENTE), candidato.perfis());
    }

    @Test
    void deveAplicarMesmoFiltroNaListagemENaContagem() {
        service.findByNome("Ana", 2, 5);
        assertEquals("Ana", usuarios.filtroLista);
        assertEquals("Ana", usuarios.filtroContagem);
        assertEquals(2, usuarios.page);
        assertEquals(5, usuarios.pageSize);
    }

    @Test
    void deveRejeitarPaginacaoInvalida() {
        assertThrows(ValidationException.class, () -> service.findAll(-1, 10));
        assertThrows(ValidationException.class, () -> service.findAll(0, 0));
        assertThrows(ValidationException.class, () -> service.findAll(0, 101));
        assertThrows(ValidationException.class, () -> service.findAll(Integer.MAX_VALUE, 10));
    }

    @Test
    void deveBuscarCpfApenasQuandoUsuarioExiste() {
        assertThrows(ResourceNotFoundException.class, () -> service.findByCpf("12345678901"));
        usuarios.porCpf = service.create(dto(Set.of(2L)));
        assertEquals("ana@example.com", service.findByCpf("12345678901").login());
        assertThrows(ResourceNotFoundException.class, () -> service.findByCpf("inexistente"));
    }

    @Test
    void deveAtualizarContaSemAlterarSenhaQuandoOmitida() {
        usuarios.porId = service.create(dto(Set.of(2L)));
        String hashOriginal = usuarios.porId.getSenha();
        var resposta = service.update(1L, new UsuarioUpdateDTO(null, false, Set.of(2L)));
        assertFalse(resposta.ativo());
        assertEquals(Set.of(Perfil.CLIENTE), resposta.perfis());
        assertEquals(hashOriginal, usuarios.porId.getSenha());
        assertEquals("ana@example.com", resposta.login());
        assertSame(pessoas.pessoa, usuarios.porId.getPessoa());
    }

    @Test
    void deveAtualizarDadosDaPessoaExistenteAoCriarUsuarioPorCpf() {
        Usuario usuario = service.create(dto("Ana Clara", "ana.clara@example.com", "12345678901", Set.of(2L)));

        assertSame(pessoas.pessoa, usuario.getPessoa());
        assertEquals("Ana Clara", pessoas.pessoa.getNome());
        assertEquals("ana.clara@example.com", pessoas.pessoa.getEmail());
        assertEquals("12345678901", pessoas.pessoa.getCpf());
        assertEquals("ana.clara@example.com", usuario.getLogin());
    }

    @Test
    void deveAtualizarHashQuandoSenhaInformada() {
        usuarios.porId = service.create(dto(Set.of(2L)));
        String novaSenha = "Uma nova senha para teste!";
        service.update(1L, new UsuarioUpdateDTO(novaSenha, true, Set.of(2L)));
        assertTrue(service.senhaService.verificar("ana@example.com", novaSenha, usuarios.porId.getSenha()));
        assertFalse(service.senhaService.verificar("ana@example.com", dto(Set.of(2L)).senha(),
                usuarios.porId.getSenha()));
    }

    @Test
    void deveRejeitarAlteracaoInvalidaSemModificarConta() {
        usuarios.porId = service.create(dto(Set.of(2L)));
        assertThrows(ValidationException.class,
                () -> service.update(1L, new UsuarioUpdateDTO(null, false, Set.of(99L))));
        assertThrows(ValidationException.class,
                () -> service.update(1L, new UsuarioUpdateDTO("               ", false, Set.of(2L))));
        assertTrue(usuarios.porId.isAtivo());
        assertEquals(Set.of(Perfil.CLIENTE), usuarios.porId.getPerfis());
    }

    @Test
    void deveExcluirApenasUsuario() {
        usuarios.porId = service.create(dto(Set.of(2L)));
        service.delete(1L);
        assertSame(usuarios.porId, usuarios.excluido);
        assertNotNull(pessoas.pessoa);
        assertEquals("ana@example.com", pessoas.pessoa.getEmail());
    }

    @Test
    void deveRetornarNaoEncontradoParaIdSemConta() {
        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
        assertThrows(ResourceNotFoundException.class,
                () -> service.update(1L, new UsuarioUpdateDTO(null, true, Set.of(2L))));
        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L));
    }

    private static class PessoasEmMemoria extends PessoaRepository {
        Pessoa pessoa;

        @Override
        public Pessoa findByCpf(String cpf) {
            if (pessoa == null) {
                return null;
            }
            return cpf.equals(pessoa.getCpf()) ? pessoa : null;
        }

        @Override
        public Pessoa findById(Long id) {
            return pessoa;
        }

        @Override
        public Pessoa findByCpfExceptId(String cpf, Long idPessoa) {
            Pessoa candidata = findByCpf(cpf);
            if (candidata == null) {
                return null;
            }
            if (idPessoa != null && idPessoa.equals(candidata.getId())) {
                return null;
            }
            return candidata;
        }

        @Override
        public Pessoa findByEmailExceptId(String email, Long idPessoa) {
            if (pessoa == null) {
                return null;
            }
            if (!email.equals(pessoa.getEmail())) {
                return null;
            }
            if (idPessoa != null && idPessoa.equals(pessoa.getId())) {
                return null;
            }
            return pessoa;
        }
    }

    private static class UsuariosEmMemoria extends UsuarioRepository {
        Usuario porPessoa;
        Usuario porCpf;
        Usuario porLogin;
        Usuario persistido;
        Usuario porId;
        Usuario excluido;
        List<Usuario> linhas = List.of();
        long total;
        String filtroLista;
        String filtroContagem;
        int page;
        int pageSize;

        @Override
        public List<Usuario> listarUsuarios(String nome, int page, int pageSize) {
            this.filtroLista = nome;
            this.page = page;
            this.pageSize = pageSize;
            return linhas;
        }

        @Override
        public long contarUsuarios(String nome) {
            filtroContagem = nome;
            return total;
        }

        @Override
        public Usuario findById(Long id) {
            return porId;
        }

        @Override
        public void delete(Usuario usuario) {
            excluido = usuario;
        }

        @Override
        public Usuario findByPessoaId(Long id) {
            return porPessoa;
        }

        @Override
        public Usuario findByCpf(String cpf) {
            if (porCpf == null || porCpf.getPessoa() == null) {
                return null;
            }
            return cpf.equals(porCpf.getPessoa().getCpf()) ? porCpf : null;
        }

        @Override
        public Usuario findByLogin(String login) {
            return porLogin;
        }

        @Override
        public void persistAndFlush(Usuario usuario) {
            persistido = usuario;
        }
    }
}

