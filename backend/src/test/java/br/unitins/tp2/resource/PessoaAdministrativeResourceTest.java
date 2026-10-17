package br.unitins.tp2.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;
import java.util.UUID;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

@QuarkusTest
class PessoaAdministrativeResourceTest {

    @Test
    @TestSecurity(user = "admin", roles = "ADMINISTRADOR")
    void cadastraPessoaJuridicaComEnderecoEstruturado() {
        String cnpj = String.format("%014d", Math.abs(java.util.UUID.randomUUID().getMostSignificantBits()) % 100000000000000L);
        given()
                .contentType("application/json")
                .body("""
                        {
                          "tipoPessoa": "JURIDICA",
                          "nome": "Loja de Carros Ltda",
                          "email": "cadastro-pj@example.com",
                          "cnpj": "%s",
                          "razaoSocial": "Loja de Carros Ltda",
                          "nomeFantasia": "Loja Carros",
                          "endereco": {
                            "cep": "77001-000",
                            "logradouro": "Praca dos Girassois",
                            "numero": "100",
                            "bairro": "Plano Diretor Sul",
                            "cidade": "Cidade de Teste",
                            "estado": "TO"
                          }
                        }
                        """.formatted(cnpj))
                .when().post("/admin/pessoas")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("tipoPessoa", equalTo("JURIDICA"))
                .body("cnpj", equalTo(cnpj))
                .body("endereco.cidade", equalTo("Cidade de Teste"))
                .body("endereco.municipio", org.hamcrest.Matchers.nullValue())
                .body("endereco.estado", equalTo("TO"));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADMINISTRADOR")
    void rejeitaTipoDePessoaInvalidoNaAtualizacao() {
        String suffix = UUID.randomUUID().toString();
        String cnpj = String.format("%014d", Math.abs(UUID.randomUUID().getMostSignificantBits()) % 100000000000000L);
        Integer id = given().contentType("application/json").body("""
                {"tipoPessoa":"JURIDICA","nome":"Loja %s","email":"loja-%s@example.com",
                 "cnpj":"%s","razaoSocial":"Loja %s"}
                """.formatted(suffix, suffix, cnpj, suffix))
                .post("/admin/pessoas").then().statusCode(201).extract().path("id");

        given().contentType("application/json").body("""
                {"tipoPessoa":"INVALIDA","nome":"Loja","email":"loja-%s@example.com",
                 "cnpj":"%s","razaoSocial":"Loja"}
                """.formatted(suffix, cnpj))
                .when().put("/admin/pessoas/{id}", id)
                .then().statusCode(400);
    }
}
