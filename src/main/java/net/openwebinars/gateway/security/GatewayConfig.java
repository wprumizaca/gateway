package net.openwebinars.gateway.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;

import java.util.Arrays;
import java.util.Collections;


@Configuration
//@EnableHystrix
public class GatewayConfig {

    @Autowired
    AuthenticationFilter filter;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("compras", r -> r.path("/address/**", "/client/**", "/detail/**", "/order/**",  "/status/**")
                        .filters(f -> f.filter(filter))
                        .uri("http://comprasapi:8080"))

                .route("usuarios", r -> r.path("/auth/**")
                        .filters(f -> f.filter(filter))
                        .uri("http://usuariosapi:8081"))
                .route("almacen", r -> r.path("/stocks/**", "/products/**", "/suppliers/**")
                        .filters(f -> f.filter(filter))
                        .uri("http://almacenapi:8082"))
                .build();
    }
}
