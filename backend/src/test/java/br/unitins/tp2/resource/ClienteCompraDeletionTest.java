package br.unitins.tp2.resource;

import static io.restassured.RestAssured.given;
import java.sql.Connection;
import java.util.UUID;
import javax.sql.DataSource;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

@QuarkusTest
class ClienteCompraDeletionTest {
    @Inject DataSource dataSource;

    @Test
    @TestSecurity(user = "admin", roles = "ADMINISTRADOR")
    void impedeExcluirClienteComNotaFiscalDeSaida() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String cpf = String.format("%011d", Math.abs(UUID.randomUUID().getMostSignificantBits()) % 100000000000L);
        Integer pessoaId = given().contentType("application/json").body("""
                {"tipoPessoa":"FISICA","nome":"Cliente %s","email":"cliente-%s@example.com",
                 "cpf":"%s","dataNascimento":"1990-01-01"}
                """.formatted(suffix, suffix, cpf))
                .post("/admin/pessoas").then().statusCode(201).extract().path("id");
        Integer clienteId = given().contentType("application/json").body("""
                {"pessoaId":%d,"ativo":true}
                """.formatted(pessoaId))
                .post("/admin/clientes").then().statusCode(201).extract().path("id");

        Integer produtoId;
        try (Connection connection = dataSource.getConnection();
             var statement = connection.prepareStatement("""
                     insert into Produto (descricao, valorCusto, valorVenda, ativo, dataCadastro)
                     values ('Veículo para teste', 10000.00, 12000.00, true, current_timestamp)
                     """, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) {
                keys.next();
                produtoId = keys.getInt(1);
            }
            try (var adicional = connection.prepareStatement(
                    "insert into Adicional (id, codigo, quantidadeEstoque) values (?, ?, 0)")) {
                adicional.setInt(1, produtoId);
                adicional.setString(2, "TESTE-" + suffix.substring(0, 8));
                adicional.executeUpdate();
            }
        }

        given().contentType("application/json").body("""
                {"numero":"N-%s","serie":"1","dataEmissao":"2026-01-15","entrada":false,
                 "clienteId":%d,"itens":[{"produtoId":%d,"quantidade":1,"valorUnitario":12000.00}]}
                """.formatted(suffix, clienteId, produtoId))
                .post("/admin/notas-fiscais").then().statusCode(201)
                .body("clienteId", org.hamcrest.Matchers.equalTo(clienteId))
                .body("valorTotal", org.hamcrest.Matchers.equalTo(12000.00f));

        given().delete("/admin/clientes/{id}", clienteId).then().statusCode(409);
        given().get("/admin/clientes/{id}", clienteId).then().statusCode(200);
    }
}
