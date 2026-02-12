package com.MotoForge.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    @Bean
    public Keycloak keycloakAdmin() {
        return KeycloakBuilder.builder()
                .serverUrl("http://localhost:8080")
                .realm("master") // Realm admin pour pouvoir créer des users
                .username("admin") // ton admin Keycloak
                .password("admin") // mot de passe admin
                .clientId("admin-cli")
                .build();
    }
}
