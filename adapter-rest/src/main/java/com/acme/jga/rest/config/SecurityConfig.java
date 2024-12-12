package com.acme.jga.rest.config;


import com.acme.jga.rest.pass.NoOpPasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final SecurityProperties securityProperties;
    private static final String URI_PATTERNS = "^(\\/api\\/v([0-9]{1})\\/system\\/(wakeup|versions|errors|techGaugeReset|dependencies|(.*))|\\/actuator|\\/actuator\\/(.*))|^(\\/api\\/v([0-9]{1})\\/spi/user?(.*))";

    @Bean
    public SecurityFilterChain basicAuthFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(RegexRequestMatcher.regexMatcher(URI_PATTERNS))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.NEVER));
        return http.build();
    }

    @Bean
    public SecurityFilterChain oauth2FilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(new NegatedRequestMatcher(RegexRequestMatcher.regexMatcher(URI_PATTERNS)))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(withDefaults()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.NEVER));
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(OAuth2ResourceServerProperties properties) {
        return JwtDecoders.fromIssuerLocation(properties.getJwt().getIssuerUri());
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User
                .withUsername(securityProperties.getUserName())
                .password(passwordEncoder().encode(securityProperties.getPass()))
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new NoOpPasswordEncoder();
    }

}
