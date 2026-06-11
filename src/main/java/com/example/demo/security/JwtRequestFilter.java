package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // Injecting the Printer and the Database lookup tool
    public JwtRequestFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 1. Look for the wristband in the request header
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 2. Check if they have a wristband that starts with "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Strip off the word "Bearer "
            try {
                username = jwtUtil.extractUsername(jwt); // Read the name on the wristband
            } catch (Exception e) {
                logger.error("Failed to extract or validate JWT Token");
            }
        }

        // 3. If we found a name, and they aren't already authenticated for this exact click
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Look them up in the database just to make sure the account still exists
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 4. Use the Printer tool to run the math and verify the signature/expiration
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {

                // 5. Access Granted! Write their name down in the active session ledger
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // This is the line that actually unlocks the door for the Controller
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 6. Step aside and let the request continue to the Controller (or bounce them if it failed)
        chain.doFilter(request, response);
    }
}