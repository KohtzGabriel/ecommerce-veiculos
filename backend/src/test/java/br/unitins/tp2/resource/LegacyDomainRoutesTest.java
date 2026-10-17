package br.unitins.tp2.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class LegacyDomainRoutesTest {

    @Test
    void legacyPatientAndHealthcareRoutesAreNotExposed() {
        for (String path : new String[] { "/pacientes", "/psicologos" }) {
            given().when().get(path).then().statusCode(404);
        }
    }

    @Test
    void geographicCrudRoutesAreNotExposed() {
        for (String path : new String[] { "/estados", "/municipios", "/regioes" }) {
            given().when().get(path).then().statusCode(404);
        }
    }

    @Test
    void publicLegacyCustomerRegistrationRouteIsNotExposed() {
        given().contentType("application/json").body("{}").when().post("/clientes").then().statusCode(404);
    }
}
