package com.paytm.seatreservation.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class UserAuthenticationFilter extends OncePerRequestFilter{

	
	 @Override
	    protected void doFilterInternal(
	            HttpServletRequest request,
	            HttpServletResponse response,
	            FilterChain filterChain
	    ) throws ServletException, IOException {

	        String authorizationHeader = request.getHeader("Authorization");

	        if (authorizationHeader != null
	                && authorizationHeader.startsWith("Bearer ")) {

	            String userId = authorizationHeader.substring(7).trim();

	            if (!userId.isBlank()) {

	                UsernamePasswordAuthenticationToken authentication =
	                        new UsernamePasswordAuthenticationToken(
	                                new AuthenticatedUser(userId),
	                                null,
	                                AuthorityUtils.NO_AUTHORITIES
	                        );

	                SecurityContextHolder.getContext()
	                        .setAuthentication(authentication);
	            }
	        }
	        filterChain.doFilter(request, response);
	        }
}
