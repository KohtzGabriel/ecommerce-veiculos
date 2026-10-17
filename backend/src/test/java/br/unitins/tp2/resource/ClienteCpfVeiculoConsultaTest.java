package br.unitins.tp2.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;

import java.sql.Connection;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;

@QuarkusTest
class ClienteCpfVeiculoConsultaTest {
    @Inject DataSource dataSource;

    @Test
    @TestSecurity(user = "admin", roles = "ADMINISTRADOR")
    void buscaClientePorCpf() {
        String sufixo = UUID.randomUUID().toString();
        String cpf = String.format("%011d", Math.abs(UUID.randomUUID().getMostSignificantBits()) % 100000000000L);
        Integer pessoaId = given().contentType("application/json").body("""
                {"tipoPessoa":"FISICA","nome":"Busca CPF","email":"cpf-%s@example.com","cpf":"%s","dataNascimento":"1990-01-01"}
                """.formatted(sufixo, cpf)).post("/admin/pessoas").then().statusCode(201).extract().path("id");
        Integer clienteId = given().contentType("application/json").body("""
                {"pessoaId":%d,"ativo":true}
                """.formatted(pessoaId)).post("/admin/clientes").then().statusCode(201).extract().path("id");

        given().get("/admin/clientes/cpf/{cpf}", cpf).then().statusCode(200)
                .body("id", equalTo(clienteId)).body("pessoaId", equalTo(pessoaId));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADMINISTRADOR")
    void filtraVeiculosPorMarcaModeloAnoEStatusCombinados() throws Exception {
        long marcaId = criarMarcaEVeiculo("Sedan Azul", "SEDAN", 2025, "DISPONIVEL");
        criarVeiculoExistente("SUV Branco", "SUV", 2024, "VENDIDO", marcaId);

        given().queryParam("anoModelo", 2025).queryParam("modelo", "sedan")
                .queryParam("marcaId", marcaId).queryParam("status", "DISPONIVEL")
                .get("/admin/veiculos")
                .then().statusCode(200).body("size()", equalTo(1))
                .body("[0].modelo", equalTo("SEDAN"));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADMINISTRADOR")
    void consultaVeiculoPorId() throws Exception {
        long marcaId = criarMarcaEVeiculo("Hatch Cinza", "HATCH", 2023, "DISPONIVEL");
        Integer id = ultimoVeiculoId(marcaId);
        given().get("/admin/veiculos/{id}", id).then().statusCode(200)
                .body("id", equalTo(id)).body("marcaId", equalTo((int) marcaId));
    }

    @Test
    void catalogoPublicoListaSomenteDisponiveisSemDadosDeCustoOuIdentificadores() throws Exception {
        long marcaId = criarMarcaEVeiculo("Sedan público", "PUBLICO", 2025, "DISPONIVEL");
        criarVeiculoExistente("SUV vendido", "VENDIDO", 2024, "VENDIDO", marcaId);

        given().queryParam("modelo", "PUBLICO").get("/catalogo/veiculos").then().statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].modelo", equalTo("PUBLICO"))
                .body("[0].marcaId", equalTo((int) marcaId))
                .body("[0]", not(hasKey("valorCusto")))
                .body("[0]", not(hasKey("chassi")))
                .body("[0]", not(hasKey("placa")));
    }

    private long criarMarcaEVeiculo(String descricao, String modelo, int ano, String status) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            long marcaId;
            try (var statement = connection.prepareStatement("insert into Marca (nome, dataCadastro) values (?, current_timestamp)", java.sql.Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, "Marca-" + UUID.randomUUID());
                statement.executeUpdate();
                try (var keys = statement.getGeneratedKeys()) { keys.next(); marcaId = keys.getLong(1); }
            }
            criarVeiculo(connection, descricao, modelo, ano, status, marcaId);
            return marcaId;
        }
    }

    private void criarVeiculoExistente(String descricao, String modelo, int ano, String status, long marcaId) throws Exception {
        try (Connection connection = dataSource.getConnection()) { criarVeiculo(connection, descricao, modelo, ano, status, marcaId); }
    }

    private void criarVeiculo(Connection connection, String descricao, String modelo, int ano, String status, long marcaId) throws Exception {
        long id;
        try (var statement = connection.prepareStatement("""
                insert into Produto (descricao, valorCusto, valorVenda, ativo, dataCadastro)
                values (?, 10000, 12000, true, current_timestamp)
                """, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, descricao);
            statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) { keys.next(); id = keys.getLong(1); }
        }
        try (var statement = connection.prepareStatement("""
                insert into Veiculo (id, modelo, anoFabricacao, anoModelo, cor, chassi, quilometragem, marca_id, status)
                values (?, ?, ?, ?, 'Prata', ?, 0, ?, ?)
                """)) {
            statement.setLong(1, id);
            statement.setString(2, modelo);
            statement.setInt(3, ano);
            statement.setInt(4, ano);
            statement.setString(5, UUID.randomUUID().toString().replace("-", "").substring(0, 17).toUpperCase());
            statement.setLong(6, marcaId);
            statement.setString(7, status);
            statement.executeUpdate();
        }
    }

    private Integer ultimoVeiculoId(long marcaId) throws Exception {
        try (Connection connection = dataSource.getConnection(); var statement = connection.prepareStatement("select min(id) from Veiculo where marca_id=?")) {
            statement.setLong(1, marcaId);
            try (var result = statement.executeQuery()) { result.next(); return result.getInt(1); }
        }
    }
}
