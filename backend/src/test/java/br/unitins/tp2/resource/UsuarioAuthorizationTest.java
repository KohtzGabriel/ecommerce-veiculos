package br.unitins.tp2.resource;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

@QuarkusTest
class UsuarioAuthorizationTest {

    @Test
    void listaDeUsuariosExigeAutenticacao() {
        given()
                .when().get("/usuarios")
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADMINISTRADOR")
    void administradorPodeListarUsuarios() {
        given()
                .when().get("/usuarios")
                .then().statusCode(200);
    }

    @Test
    @TestSecurity(user = "cliente", roles = "CLIENTE")
    void clienteNaoPodeListarUsuarios() {
        given()
                .when().get("/usuarios")
                .then().statusCode(403);
    }
}
