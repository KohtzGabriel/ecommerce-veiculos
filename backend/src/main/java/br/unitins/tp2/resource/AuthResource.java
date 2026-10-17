package br.unitins.tp2.resource;

import br.unitins.tp2.dto.AuthRequest;
import br.unitins.tp2.dto.AuthResponse;
import br.unitins.tp2.service.AuthService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {
    @Inject AuthService auth;

    @POST
    @Path("/login")
    public AuthResponse login(@Valid AuthRequest request) {
        return auth.login(request);
    }
}
