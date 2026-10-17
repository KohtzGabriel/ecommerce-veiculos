package br.unitins.tp2.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.sql.Connection;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import br.unitins.tp2.service.SenhaService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class AuthResourceTest {
    @Inject DataSource dataSource;
    @Inject SenhaService senhas;

    @Test
    void loginValidoEmiteTokenComPerfilDoUsuario() throws Exception {
        String login = criarUsuario(true, 1L);

        String token = given().contentType("application/json").body("""
                {"login":"%s","senha":"SenhaForte-123456"}
                """.formatted(login))
                .post("/auth/login").then().statusCode(200)
                .body("accessToken", notNullValue())
                .body("tokenType", equalTo("Bearer"))
                .body("roles[0]", equalTo("ADMINISTRADOR"))
                .extract().path("accessToken");
        given().auth().oauth2(token).get("/usuarios").then().statusCode(200);
    }

    @Test
    void loginComSenhaIncorretaNaoEmiteToken() throws Exception {
        String login = criarUsuario(true, 2L);

        given().contentType("application/json").body("""
                {"login":"%s","senha":"senha-incorreta"}
                """.formatted(login))
                .post("/auth/login")
                .then().statusCode(401);
    }

    @Test
    void usuarioInativoNaoPodeAutenticar() throws Exception {
        String login = criarUsuario(false, 2L);

        given().contentType("application/json").body("""
                {"login":"%s","senha":"SenhaForte-123456"}
                """.formatted(login))
                .post("/auth/login")
                .then().statusCode(401);
    }

    @Test
    void tokenClienteNaoAcessaRotasAdministrativas() throws Exception {
        String login = criarUsuario(true, 2L);
        String token = given().contentType("application/json").body("""
                {"login":"%s","senha":"SenhaForte-123456"}
                """.formatted(login)).post("/auth/login").then().statusCode(200)
                .body("roles[0]", equalTo("CLIENTE")).extract().path("accessToken");

        given().auth().oauth2(token).get("/usuarios").then().statusCode(403);
    }

    private String criarUsuario(boolean ativo, long perfilId) throws Exception {
        String sufixo = UUID.randomUUID().toString();
        String login = "login-" + sufixo + "@example.com";
        String hash = senhas.gerarHash(login, "SenhaForte-123456");
        try (Connection connection = dataSource.getConnection()) {
            long pessoaId;
            try (var pessoa = connection.prepareStatement("""
                    insert into Pessoa (nome, email, dataCadastro) values (?, ?, current_timestamp)
                    """, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                pessoa.setString(1, "Teste Login");
                pessoa.setString(2, login);
                pessoa.executeUpdate();
                try (var keys = pessoa.getGeneratedKeys()) { keys.next(); pessoaId = keys.getLong(1); }
            }
            try (var pessoaFisica = connection.prepareStatement(
                    "insert into PessoaFisica (id, cpf, dataNascimento) values (?, ?, DATE '1990-01-01')")) {
                pessoaFisica.setLong(1, pessoaId);
                pessoaFisica.setString(2, String.format("%011d", Math.abs(UUID.randomUUID().getMostSignificantBits()) % 100000000000L));
                pessoaFisica.executeUpdate();
            }
            try (var usuario = connection.prepareStatement("""
                    insert into Usuario (login, senha, ativo, perfis, pessoa_id, dataCadastro)
                    values (?, ?, ?, ?, ?, current_timestamp)
                    """)) {
                usuario.setString(1, login);
                usuario.setString(2, hash);
                usuario.setBoolean(3, ativo);
                usuario.setArray(4, connection.createArrayOf("BIGINT", new Object[] { perfilId }));
                usuario.setLong(5, pessoaId);
                usuario.executeUpdate();
            }
        }
        return login;
    }
}
