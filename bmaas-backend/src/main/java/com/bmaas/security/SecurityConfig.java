package com.bmaas.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                // SLA Rules endpoints (PM only)
                .requestMatchers("/api/sla-rules/**").hasRole("PROJECT_MANAGER")
                // Developer management, metrics & leaderboard
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/developers/*/link-user").hasRole("PROJECT_MANAGER")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/developers/leaderboard").hasRole("PROJECT_MANAGER")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/developers/*/metrics").hasAnyRole("PROJECT_MANAGER", "DEVELOPER")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/developers/*/assignment-history").hasAnyRole("PROJECT_MANAGER", "DEVELOPER")
                .requestMatchers("/api/developers/me/**").hasAnyRole("DEVELOPER", "PROJECT_MANAGER")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/developers/**").hasAnyRole("PROJECT_MANAGER", "TESTER", "DEVELOPER")
                .requestMatchers("/api/developers/**").hasRole("PROJECT_MANAGER")
                // Bug assignment (PM only)
                .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/bugs/*/assign").hasRole("PROJECT_MANAGER")
                .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/bugs/*/unassign").hasRole("PROJECT_MANAGER")
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/bugs/**").hasRole("PROJECT_MANAGER")
                // Bug creation and listing
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/projects/*/modules/*/bugs").hasAnyRole("PROJECT_MANAGER", "TESTER")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/projects/*/bugs/**").hasAnyRole("PROJECT_MANAGER", "TESTER")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/projects/*/bugs").hasAnyRole("PROJECT_MANAGER", "TESTER")
                // Bug status & details
                .requestMatchers("/api/bugs/**").hasAnyRole("PROJECT_MANAGER", "TESTER", "DEVELOPER")
                // Projects & Modules endpoints
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/projects/**").hasAnyRole("PROJECT_MANAGER", "TESTER", "DEVELOPER")
                .requestMatchers("/api/projects/**").hasRole("PROJECT_MANAGER")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/modules/**").hasAnyRole("PROJECT_MANAGER", "TESTER", "DEVELOPER")
                .requestMatchers("/api/modules/**").hasRole("PROJECT_MANAGER")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}