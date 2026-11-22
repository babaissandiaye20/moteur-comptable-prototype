package com.dmplus.moteurcomptable.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Remplit created_by / updated_by. L'utilisateur vient de l'en-tête HTTP « X-Utilisateur »
 * envoyé par le front (prototype : pas encore de connexion). Sans requête (démarrage) : « systeme ».
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "utilisateurCourant")
public class AuditConfig {

    public static final String ENTETE_UTILISATEUR = "X-Utilisateur";

    @Bean
    public AuditorAware<String> utilisateurCourant() {
        return () -> {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
                String u = attrs.getRequest().getHeader(ENTETE_UTILISATEUR);
                if (u != null && !u.isBlank()) {
                    return Optional.of(u.trim());
                }
            }
            return Optional.of("systeme");
        };
    }
}
