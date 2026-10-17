package br.unitins.tp2.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

@QuarkusTest
class NotaFiscalVeiculoIntegrationTest {
    @Test
    @TestSecurity(user = "admin", roles = "ADMINISTRADOR")
    void entradaCadastraCarroSaidaVendeEExclusoesRevertemEstoque() {
        String sufixo = UUID.randomUUID().toString();
        int marcaId = criarMarca(sufixo);
        int fornecedorId = criarFornecedor(sufixo);
        int notaEntrada = inserirEntrada(sufixo, fornecedorId, marcaId);
        int veiculoId = given().queryParam("modelo", "Modelo-" + sufixo)
                .get("/admin/veiculos").then().statusCode(200)
                .body("size()", equalTo(1)).extract().path("[0].id");

        int clienteId = criarCliente(sufixo);
        int notaSaida = given().contentType("application/json").body("""
                {"numero":"S-%s","serie":"1","dataEmissao":"2026-01-15","entrada":false,
                 "clienteId":%d,"itens":[{"produtoId":%d,"quantidade":1,"valorUnitario":12000.00}]}
                """.formatted(sufixo, clienteId, veiculoId))
                .post("/admin/notas-fiscais").then().statusCode(201).extract().path("id");
        given().get("/admin/veiculos/{id}", veiculoId).then().statusCode(200).body("status", equalTo("VENDIDO"));

        given().contentType("application/json").body("""
                {"numero":"S2-%s","serie":"1","dataEmissao":"2026-01-15","entrada":false,
                 "clienteId":%d,"itens":[{"produtoId":%d,"quantidade":1,"valorUnitario":12000.00}]}
                """.formatted(sufixo, clienteId, veiculoId))
                .post("/admin/notas-fiscais").then().statusCode(409);

        given().delete("/admin/notas-fiscais/{id}", notaSaida).then().statusCode(204);
        given().get("/admin/veiculos/{id}", veiculoId).then().statusCode(200).body("status", equalTo("DISPONIVEL"));
        given().delete("/admin/notas-fiscais/{id}", notaEntrada).then().statusCode(204);
        given().get("/admin/veiculos/{id}", veiculoId).then().statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADMINISTRADOR")
    void naoRemoveNotaDeEntradaQuandoCarroJaFoiVendido() {
        String sufixo = UUID.randomUUID().toString();
        int marcaId = criarMarca(sufixo);
        int fornecedorId = criarFornecedor(sufixo);
        int notaEntrada = inserirEntrada(sufixo, fornecedorId, marcaId);
        int veiculoId = given().queryParam("modelo", "Modelo-" + sufixo)
                .get("/admin/veiculos").then().statusCode(200).extract().path("[0].id");
        int clienteId = criarCliente(sufixo);
        given().contentType("application/json").body("""
                {"numero":"SV-%s","serie":"1","dataEmissao":"2026-01-15","entrada":false,
                 "clienteId":%d,"itens":[{"produtoId":%d,"quantidade":1,"valorUnitario":12000.00}]}
                """.formatted(sufixo, clienteId, veiculoId))
                .post("/admin/notas-fiscais").then().statusCode(201);

        given().delete("/admin/notas-fiscais/{id}", notaEntrada).then().statusCode(409);
        given().get("/admin/veiculos/{id}", veiculoId).then().statusCode(200).body("status", equalTo("VENDIDO"));
        given().get("/admin/notas-fiscais/{id}", notaEntrada).then().statusCode(200);
    }

    private int criarMarca(String sufixo) {
        return given().contentType("application/json").body("""
                {"nome":"Marca-%s"}
                """.formatted(sufixo)).post("/admin/marcas").then().statusCode(201).extract().path("id");
    }

    private int criarFornecedor(String sufixo) {
        int pessoaId = given().contentType("application/json").body("""
                {"tipoPessoa":"JURIDICA","nome":"Fornecedor %s","email":"forn-%s@example.com",
                 "cnpj":"%s","razaoSocial":"Fornecedor %s Ltda"}
                """.formatted(sufixo, sufixo, cnpj(), sufixo))
                .post("/admin/pessoas").then().statusCode(201).extract().path("id");
        return given().contentType("application/json").body("""
                {"pessoaJuridicaId":%d,"ativo":true}
                """.formatted(pessoaId)).post("/admin/fornecedores").then().statusCode(201).extract().path("id");
    }

    private int criarCliente(String sufixo) {
        int pessoaId = given().contentType("application/json").body("""
                {"tipoPessoa":"FISICA","nome":"Cliente %s","email":"cli-%s@example.com",
                 "cpf":"%s","dataNascimento":"1990-01-01"}
                """.formatted(sufixo, sufixo, cpf())).post("/admin/pessoas").then().statusCode(201).extract().path("id");
        return given().contentType("application/json").body("""
                {"pessoaId":%d,"ativo":true}
                """.formatted(pessoaId)).post("/admin/clientes").then().statusCode(201).extract().path("id");
    }

    private int inserirEntrada(String sufixo, int fornecedorId, int marcaId) {
        return given().contentType("application/json").body("""
                {"numero":"E-%s","serie":"1","dataEmissao":"2026-01-15","entrada":true,
                 "fornecedorId":%d,"itens":[{"quantidade":1,"valorUnitario":12000.00,
                 "veiculoNovo":{"descricao":"Veículo %s","valorCusto":10000.00,"valorVenda":12000.00,
                 "modelo":"Modelo-%s","anoFabricacao":2025,"anoModelo":2026,"cor":"Prata",
                 "chassi":"%s","quilometragem":0,"marcaId":%d}}]}
                """.formatted(sufixo, fornecedorId, sufixo, sufixo,
                        UUID.randomUUID().toString().replace("-", "").substring(0, 17).toUpperCase(), marcaId))
                .post("/admin/notas-fiscais").then().statusCode(201).extract().path("id");
    }

    private String cpf() { return String.format("%011d", Math.abs(UUID.randomUUID().getMostSignificantBits()) % 100000000000L); }
    private String cnpj() { return String.format("%014d", Math.abs(UUID.randomUUID().getMostSignificantBits()) % 100000000000000L); }
}
