package br.unitins.tp2.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import br.unitins.tp2.model.Adicional;
import br.unitins.tp2.model.Cliente;
import br.unitins.tp2.model.Endereco;
import br.unitins.tp2.model.Fornecedor;
import br.unitins.tp2.model.Marca;
import br.unitins.tp2.model.Perfil;
import br.unitins.tp2.model.PessoaFisica;
import br.unitins.tp2.model.PessoaJuridica;
import br.unitins.tp2.model.StatusVeiculo;
import br.unitins.tp2.model.Telefone;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.model.Veiculo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class DemoDataInitializer {
    public static final String ADMIN_LOGIN = "admin@demo.veiculos.local";
    public static final String CLIENT_LOGIN = "cliente@demo.veiculos.local";
    public static final String ADMIN_PASSWORD = "DemoAdmin@2026";
    public static final String CLIENT_PASSWORD = "DemoCliente@2026";

    @Inject EntityManager entityManager;
    @Inject SenhaService senhas;
    @ConfigProperty(name = "app.demo-data.enabled", defaultValue = "false")
    boolean enabled;

    @Transactional
    void onStart(@Observes StartupEvent ignored) {
        if (enabled) seedIfNeeded();
    }

    @Transactional
    public void seedIfNeeded() {
        if (!enabled || demoAdminExists()) return;

        PessoaFisica administrador = pessoaFisica(
                "Administrador Demonstração", ADMIN_LOGIN, "52998224725", "630000000", "77001000");
        criarUsuario(administrador, ADMIN_LOGIN, ADMIN_PASSWORD, Perfil.ADMINISTRADOR);

        PessoaFisica pessoaCliente = pessoaFisica(
                "Cliente Demonstração", CLIENT_LOGIN, "11144477735", "630000001", "77020000");
        entityManager.persist(pessoaCliente);

        Cliente cliente = new Cliente();
        cliente.setPessoa(pessoaCliente);
        cliente.setAtivo(true);
        entityManager.persist(cliente);
        criarUsuario(pessoaCliente, CLIENT_LOGIN, CLIENT_PASSWORD, Perfil.CLIENTE);

        PessoaJuridica pessoaFornecedor = new PessoaJuridica();
        pessoaFornecedor.setNome("Fornecedor Demonstração");
        pessoaFornecedor.setEmail("fornecedor@demo.veiculos.local");
        pessoaFornecedor.setCnpj("11222333000181");
        pessoaFornecedor.setRazaoSocial("Auto Peças e Veículos Demonstração Ltda.");
        pessoaFornecedor.setNomeFantasia("Auto Demo Fornecedor");
        entityManager.persist(pessoaFornecedor);

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setPessoaJuridica(pessoaFornecedor);
        fornecedor.setAtivo(true);
        entityManager.persist(fornecedor);

        Marca toyota = marca("Toyota");
        Marca honda = marca("Honda");
        Marca volkswagen = marca("Volkswagen");

        entityManager.persist(veiculo(toyota, "Corolla Cross XRE", "SUV híbrido, automático, completo.",
                2024, 2025, "Cinza", "9BRBDWHE0G0123456", "145000.00", "179990.00", 12000));
        entityManager.persist(veiculo(honda, "Civic Touring", "Sedã turbo com pacote de assistência ao condutor.",
                2023, 2024, "Preto", "9BWZZZ377VT004251", "158000.00", "189900.00", 18500));
        entityManager.persist(veiculo(volkswagen, "T-Cross Highline", "SUV compacto com teto panorâmico.",
                2024, 2025, "Branco", "9BWZZZ377VT004252", "132000.00", "159900.00", 7600));

        Adicional tapete = new Adicional();
        tapete.setCodigo("DEMO-TAP-001");
        tapete.setDescricao("Jogo de tapetes automotivos premium");
        tapete.setValorCusto(new BigDecimal("180.00"));
        tapete.setValorVenda(new BigDecimal("299.90"));
        tapete.setQuantidadeEstoque(8);
        tapete.setAtivo(true);
        entityManager.persist(tapete);

        entityManager.flush();
    }

    private boolean demoAdminExists() {
        return entityManager.createQuery("select count(u) from Usuario u where u.login = :login", Long.class)
                .setParameter("login", ADMIN_LOGIN)
                .getSingleResult() > 0;
    }

    private PessoaFisica pessoaFisica(String nome, String email, String cpf, String telefone, String cep) {
        PessoaFisica pessoa = new PessoaFisica();
        pessoa.setNome(nome);
        pessoa.setEmail(email);
        pessoa.setCpf(cpf);
        pessoa.setDataNascimento(LocalDate.of(1990, 5, 15));
        pessoa.setGenero("Não informado");
        pessoa.setEndereco(endereco(cep));
        Telefone contato = new Telefone();
        contato.setCodigoArea("63");
        contato.setNumero(telefone);
        contato.setWhatsapp(true);
        pessoa.setTelefones(List.of(contato));
        return pessoa;
    }

    private Endereco endereco(String cep) {
        Endereco endereco = new Endereco();
        endereco.setCep(cep);
        endereco.setLogradouro("Avenida Demo");
        endereco.setNumero("100");
        endereco.setComplemento("Sala de demonstração");
        endereco.setBairro("Plano Diretor");
        endereco.setCidade("Palmas");
        endereco.setEstado("TO");
        return endereco;
    }

    private void criarUsuario(PessoaFisica pessoa, String login, String senha, Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setPessoa(pessoa);
        usuario.setLogin(login);
        usuario.setSenha(senhas.gerarHash(login, senha));
        usuario.setAtivo(true);
        usuario.setPerfis(Set.of(perfil));
        entityManager.persist(usuario);
    }

    private Marca marca(String nome) {
        Marca marca = new Marca();
        marca.setNome(nome);
        entityManager.persist(marca);
        return marca;
    }

    private Veiculo veiculo(Marca marca, String modelo, String descricao, int anoFabricacao, int anoModelo,
            String cor, String chassi, String custo, String venda, int quilometragem) {
        Veiculo veiculo = new Veiculo();
        veiculo.setMarca(marca);
        veiculo.setModelo(modelo);
        veiculo.setDescricao(descricao);
        veiculo.setAnoFabricacao(anoFabricacao);
        veiculo.setAnoModelo(anoModelo);
        veiculo.setCor(cor);
        veiculo.setChassi(chassi);
        veiculo.setQuilometragem(quilometragem);
        veiculo.setValorCusto(new BigDecimal(custo));
        veiculo.setValorVenda(new BigDecimal(venda));
        veiculo.setAtivo(true);
        veiculo.setStatus(StatusVeiculo.DISPONIVEL);
        return veiculo;
    }
}
