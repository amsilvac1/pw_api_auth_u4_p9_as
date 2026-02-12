package uce.edu.ec.api.auth.interfaces;

import java.time.Instant;
import java.util.Set;

import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uce.edu.ec.api.auth.application.UsuarioService;
import uce.edu.ec.api.auth.application.representation.UsuarioRepresentation;

@Path("/auth")
public class AuthResource {

    @Inject
    UsuarioService usuarioService;

    @GET
    @Path("/token")
    @Produces(MediaType.APPLICATION_JSON)
    public Response token(
            @QueryParam("usuario") String user,
            @QueryParam("password") String password) {

        UsuarioRepresentation usuario = usuarioService.validarCredenciales(user, password);

        if (usuario != null) {
            String issuer = "citas-auth";
            long ttl = 3600;

            Instant now = Instant.now();
            Instant exp = now.plusSeconds(ttl);

            String jwt = Jwt.issuer(issuer)
                    .subject(user)
                    .groups(Set.of(usuario.rol))
                    .issuedAt(now)
                    .expiresAt(exp)
                    .sign();

            TokenResponse resp = new TokenResponse(jwt, exp.getEpochSecond(), usuario.rol);

            return Response.ok(resp).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

    }

    public static class TokenResponse {
        public String accessToken;
        public long expiresAt;
        public String role;

        public TokenResponse() {
        }

        public TokenResponse(String accessToken, long expiresAt, String role) {
            this.accessToken = accessToken;
            this.expiresAt = expiresAt;
            this.role = role;
        }
    }
}
