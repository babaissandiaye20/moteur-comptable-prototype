package com.dmplus.moteurcomptable.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Autorise le front (Vercel) à appeler l'API (Render), qui est sur un autre domaine.
 * Les adresses autorisées viennent de la variable MOTEUR_CORS_ORIGINES, séparées par des virgules.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] origines;

    public CorsConfig(@Value("${moteur.cors.origines}") String origines) {
        this.origines = origines.split("\\s*,\\s*");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(origines)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "X-Utilisateur");
    }
}
