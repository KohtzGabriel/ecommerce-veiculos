package br.unitins.tp2.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import java.sql.Connection;
import java.util.UUID;
import javax.sql.DataSource;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

@QuarkusTest
class FornecedorAdministrativeResourceTest {
    @Inject DataSource dataSource;

    @Test
    @TestSecurity(user = "admin", roles = "ADMINISTRADOR")
    void impedeRemoverFornecedorReferenciadoPorNotaFiscal() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String cnpj = String.format("%014d", Math.abs(UUID.randomUUID().getMostSignificantBits()) % 100000000000000L);
        Integer pessoaId = given().contentType("application/json").body("""
                {"tipoPessoa":"JURIDICA","nome":"Forn %s","email":"forn-%s@example.com",
                 "cnpj":"%s","razaoSocial":"Forn %s"}
                """.formatted(suffix, suffix, cnpj, suffix))
                .post("/admin/pessoas").then().statusCode(201).extract().path("id");
        Integer fornecedorId = given().contentType("application/json")
                .body("""
                        {"pessoaJuridicaId":%d,"ativo":true}
                        """.formatted(pessoaId))
                .post("/admin/fornecedores").then().statusCode(201).extract().path("id");

        try (Connection connection = dataSource.getConnection();
             var statement = connection.prepareStatement("""
                     insert into NotaFiscal (numero, serie, dataEmissao, entrada, valorTotal, fornecedor_id, dataCadastro)
                     values (?, ?, current_date, true, 10.00, ?, current_timestamp)
                     """)) {
            statement.setString(1, "NF-" + suffix);
            statement.setString(2, "1");
            statement.setLong(3, fornecedorId.longValue());
            statement.executeUpdate();
        }

        given().delete("/admin/fornecedores/{id}", fornecedorId)
                .then().statusCode(409).body("status", equalTo(409));
    }
}
