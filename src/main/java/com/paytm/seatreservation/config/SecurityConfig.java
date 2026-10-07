package com.paytm.seatreservation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.paytm.seatreservation.security.UserAuthenticationFilter;

@Configuration
public class SecurityConfig {

	private final UserAuthenticationFilter userAuthenticationFilter;

	public SecurityConfig(UserAuthenticationFilter userAuthenticationFilter) {
		this.userAuthenticationFilter = userAuthenticationFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable())

				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authorizeHttpRequests(auth -> auth

						.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()

						.requestMatchers("/shows/**").authenticated()

						.anyRequest().permitAll())

				.addFilterBefore(userAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
