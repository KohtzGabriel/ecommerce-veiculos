package br.unitins.tp2.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

@QuarkusTest
class PessoaComVinculosTest {
    @Test
    @TestSecurity(user = "admin", roles = "ADMINISTRADOR")
    void criaPessoaFisicaComClienteEUsuarioNaMesmaPessoa() {
        String suffix = UUID.randomUUID().toString();
        String cpf = String.format("%011d", Math.floorMod(UUID.randomUUID().getMostSignificantBits(), 100000000000L));
        String email = "vinculo-" + suffix + "@example.com";
        Integer pessoaId = given().contentType("application/json").body("""
                {"tipoPessoa":"FISICA","nome":"Pessoa com vínculos","email":"%s",
                 "cpf":"%s","dataNascimento":"1990-01-01","cadastrarCliente":true,
                 "usuario":{"senha":"SenhaDeTeste@2026","perfilId":2}}
                """.formatted(email, cpf))
                .post("/admin/pessoas").then().statusCode(201).extract().path("id");
        given().get("/admin/clientes/cpf/{cpf}", cpf).then().statusCode(200).body("pessoaId", equalTo(pessoaId));
        given().get("/usuarios/cpf/{cpf}", cpf).then().statusCode(200).body("login", equalTo(email));
        given().contentType("application/json").body("""
                {"login":"%s","senha":"SenhaDeTeste@2026"}
                """.formatted(email)).post("/auth/login").then().statusCode(200)
                .body("roles", hasItem("CLIENTE")).body("accessToken", not(emptyString()));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADMINISTRADOR")
    void criaPessoaJuridicaComClienteEFornecedor() {
        String suffix = UUID.randomUUID().toString();
        String cnpj = String.format("%014d", Math.floorMod(UUID.randomUUID().getMostSignificantBits(), 100000000000000L));
        Integer pessoaId = given().contentType("application/json").body("""
                {"tipoPessoa":"JURIDICA","nome":"Empresa com vínculos","email":"empresa-%s@example.com",
                 "cnpj":"%s","razaoSocial":"Empresa teste","cadastrarCliente":true,"cadastrarFornecedor":true}
                """.formatted(suffix, cnpj))
                .post("/admin/pessoas").then().statusCode(201).extract().path("id");
        given().get("/admin/fornecedores").then().statusCode(200).body("pessoaJuridicaId", hasItem(pessoaId));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADMINISTRADOR")
    void rejeitaFornecedorFisicoSemPersistirPessoa() {
        String email = "invalido-" + UUID.randomUUID() + "@example.com";
        given().contentType("application/json").body("""
                {"tipoPessoa":"FISICA","nome":"Pessoa inválida","email":"%s",
                 "cpf":"12345678901","dataNascimento":"1990-01-01","cadastrarFornecedor":true}
                """.formatted(email)).post("/admin/pessoas").then().statusCode(400);
        given().get("/admin/pessoas").then().statusCode(200).body("email", not(hasItem(email)));
    }
}
