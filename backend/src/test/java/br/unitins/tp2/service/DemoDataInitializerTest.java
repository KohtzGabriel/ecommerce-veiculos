package br.unitins.tp2.service;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
@TestProfile(DemoDataInitializerTest.DemoProfile.class)
class DemoDataInitializerTest {
    private static final String ADMIN_LOGIN = "admin@demo.veiculos.local";
    private static final String ADMIN_PASSWORD = "DemoAdmin@2026";

    @Inject DemoDataInitializer initializer;
    @Inject EntityManager entityManager;

    @Test
    void inicializaContaDeAdministracaoECatalogoSemDuplicarAoExecutarNovamente() {
        String token = given().contentType("application/json").body("""
                {"login":"%s","senha":"%s"}
                """.formatted(ADMIN_LOGIN, ADMIN_PASSWORD))
                .post("/auth/login").then().statusCode(200)
                .body("accessToken", notNullValue())
                .body("roles[0]", equalTo("ADMINISTRADOR"))
                .extract().path("accessToken");

        given().auth().oauth2(token).get("/admin/veiculos").then().statusCode(200);
        given().get("/catalogo/veiculos").then().statusCode(200).body("$", hasSize(3));

        initializer.seedIfNeeded();

        assertEquals(3L, entityManager.createQuery("select count(v) from Veiculo v", Long.class).getSingleResult());
        assertEquals(2L, entityManager.createQuery("select count(u) from Usuario u", Long.class).getSingleResult());
        assertEquals(1L, entityManager.createQuery("select count(c) from Cliente c", Long.class).getSingleResult());
        assertEquals(1L, entityManager.createQuery("select count(f) from Fornecedor f", Long.class).getSingleResult());
        assertEquals(3L, entityManager.createQuery("select count(m) from Marca m", Long.class).getSingleResult());
        assertEquals(1L, entityManager.createQuery("select count(a) from Adicional a", Long.class).getSingleResult());
    }

    @Test
    void contaDeClienteDeDemonstracaoAutenticaComPerfilDeCliente() {
        given().contentType("application/json").body("""
                {"login":"cliente@demo.veiculos.local","senha":"DemoCliente@2026"}
                """).post("/auth/login").then().statusCode(200)
                .body("roles[0]", equalTo("CLIENTE"));
    }

    public static class DemoProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "app.demo-data.enabled", "true",
                    "quarkus.datasource.jdbc.url", "jdbc:h2:mem:demoSeedTest;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                    "quarkus.hibernate-orm.schema-management.strategy", "update",
                    "quarkus.hibernate-orm.sql-load-script", "no-file");
        }
    }
}
