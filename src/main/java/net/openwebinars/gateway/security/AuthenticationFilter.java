package net.openwebinars.gateway.security;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@RefreshScope
@Component
public class AuthenticationFilter implements GatewayFilter {

    @Autowired
    private RouterValidator routerValidator;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (routerValidator.isSecured.test(request)) {
            if (this.isAuthMissing(request))
                return this.onError(exchange, "Authorization header is missing in request", HttpStatus.UNAUTHORIZED);

            final String token = this.getAuthHeader(request);

            if (jwtUtil.isInvalid(token))
                return this.onError(exchange, "Authorization header is invalid", HttpStatus.UNAUTHORIZED);

            this.populateRequestWithHeaders(exchange, token);

            // Nuevas líneas para redirigir según el rol
            if (routerValidator.hasRoleMaster.test(request))
                exchange.getRequest().mutate().path("/users/**")
                        .path( "/stocks/**")
                        .path( "/products/**")
                        .path( "/suppliers/**")
                        .path( "/address/**")
                        .path( "/client/**")
                        .path( "/detail/**")
                        .path( "/order/**")
                        .path( "/status/**").build();

            if (routerValidator.hasRoleAdminUser.test(request))
                exchange.getRequest().mutate().path("/users/**").build();

            if (routerValidator.hasRoleAdminApp.test(request))
                exchange.getRequest().mutate().path("/products/**").build();


            if (routerValidator.hasRoleClient.test(request))
                exchange.getRequest().mutate().path("/address/**")
                        .path( "/client/**")
                        .path( "/detail/**")
                        .path( "/order/**")
                        .path( "/status/**").build();

            // Restricción por defecto para roles no autorizados
            return onAuthorizationError(exchange, "You are not authorized to access this resource.");
        }
        return chain.filter(exchange);
    }


    /*PRIVATE*/

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    private String getAuthHeader(ServerHttpRequest request) {
        return request.getHeaders().getOrEmpty("Authorization").get(0);
    }

    private boolean isAuthMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }

    private void populateRequestWithHeaders(ServerWebExchange exchange, String token) {
        Claims claims = jwtUtil.getAllClaimsFromToken(token);
        List<String> roles = claims.get("role", List.class);

        String role = null;
        if (roles != null && !roles.isEmpty()) {
            role = roles.get(0);
        }

        exchange.getRequest().mutate()
                .header("id", String.valueOf(claims.get("id")))
                .header("role", role)
                .build();
    }

    private Mono<Void> onAuthorizationError(ServerWebExchange exchange, String errorMessage) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.TEXT_PLAIN);
        response.getHeaders().add("WWW-Authenticate", "Bearer error=\"" + errorMessage + "\"");
        return response.setComplete();
    }
}