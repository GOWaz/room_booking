package com.example.room_booking.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

//    {
//        auth.requestMatchers("/api/v1/auth/**")
//                .permitAll()
//                .anyRequest()
//                .authenticated()
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (no auth)
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(
                                "/api/auth/**",
                                "/h2-console/**",       // H2 console
                                "/v3/api-docs/**",      // OpenAPI docs
                                "/swagger-ui/**",       // Swagger UI
                                "/actuator/health",      // Health check
                                "/actuator/prometheus"
                        ).permitAll()

                        // Room endpoints
                        .requestMatchers(HttpMethod.GET, "/rooms").permitAll()  // Anyone can view books
                        .requestMatchers(HttpMethod.POST, "/rooms/**").hasAuthority("ADMIN")

                        // Booking endpoints
                        .requestMatchers(HttpMethod.GET, "/bookings/**").hasAnyAuthority("USER", "ADMIN")  // Users & admins can view
                        .requestMatchers(HttpMethod.POST, "/bookings").hasAnyAuthority("ADMIN", "USER")
                        .requestMatchers(HttpMethod.PUT, "/bookings/**").hasAnyAuthority("ADMIN", "USER")  // Users can borrow/return

                        // All other requests require authentication
                        .anyRequest().authenticated())
                .sessionManagement(httpSecuritySessionManagementConfigurer ->
                        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}