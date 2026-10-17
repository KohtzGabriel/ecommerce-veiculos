package br.unitins.tp2.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

@QuarkusTest
class ClientePessoaExistenteTest {
    @Test
    @TestSecurity(user = "admin", roles = "ADMINISTRADOR")
    void clientePodeAssociarPessoaJuridicaExistenteSemCriarDuplicata() {
        String suffix = UUID.randomUUID().toString();
        String cnpj = String.format("%014d", Math.abs(UUID.randomUUID().getMostSignificantBits()) % 100000000000000L);
        Integer pessoaId = given().contentType("application/json").body("""
                {"tipoPessoa":"JURIDICA","nome":"Empresa %s","email":"empresa-%s@example.com",
                 "cnpj":"%s","razaoSocial":"Empresa %s"}
                """.formatted(suffix, suffix, cnpj, suffix))
                .post("/admin/pessoas").then().statusCode(201).extract().path("id");

        Integer clienteId = given().contentType("application/json").body("""
                {"pessoaId":%d,"ativo":true}
                """.formatted(pessoaId))
                .when().post("/admin/clientes").then().statusCode(201)
                .body("pessoaId", equalTo(pessoaId)).body("tipoPessoa", equalTo("JURIDICA"))
                .extract().path("id");

        given().get("/admin/clientes/{id}", clienteId).then().statusCode(200)
                .body("pessoaId", equalTo(pessoaId)).body("tipoPessoa", equalTo("JURIDICA"));

        given().when().get("/admin/pessoas/{id}", pessoaId).then().statusCode(200)
                .body("id", equalTo(pessoaId.intValue())).body("tipoPessoa", equalTo("JURIDICA"));

        given().contentType("application/json").body("""
                {"pessoaId":%d,"ativo":true}
                """.formatted(pessoaId))
                .post("/admin/clientes").then().statusCode(409);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADMINISTRADOR")
    void clientePodeAssociarPessoaFisicaExistente() {
        String suffix = UUID.randomUUID().toString();
        String cpf = String.format("%011d", Math.abs(UUID.randomUUID().getMostSignificantBits()) % 100000000000L);
        Integer pessoaId = given().contentType("application/json").body("""
                {"tipoPessoa":"FISICA","nome":"Pessoa %s","email":"pessoa-%s@example.com",
                 "cpf":"%s","dataNascimento":"1990-01-01"}
                """.formatted(suffix, suffix, cpf))
                .post("/admin/pessoas").then().statusCode(201).extract().path("id");

        given().contentType("application/json").body("""
                {"pessoaId":%d,"ativo":true}
                """.formatted(pessoaId))
                .post("/admin/clientes").then().statusCode(201)
                .body("pessoaId", equalTo(pessoaId)).body("tipoPessoa", equalTo("FISICA"));
    }
}
