package net.openwebinars.gateway.security;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/auth/register",
            "/auth/login"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));

    // Nuevas líneas para validación de roles
    public Predicate<ServerHttpRequest> hasRoleMaster =
            request -> request.getHeaders().containsKey("role")
                    && request.getHeaders().getFirst("role").equals("MASTER");

    public Predicate<ServerHttpRequest> hasRoleAdminApp =
            request -> request.getHeaders().containsKey("role")
                    && request.getHeaders().getFirst("role").equals("ADMIN_APP");

    public Predicate<ServerHttpRequest> hasRoleAdminUser =
            request -> request.getHeaders().containsKey("role")
                    && request.getHeaders().getFirst("role").equals("ADMIN_USER");

    public Predicate<ServerHttpRequest> hasRoleClient =
            request -> request.getHeaders().containsKey("role")
                    && request.getHeaders().getFirst("role").equals("CLIENT");

}
