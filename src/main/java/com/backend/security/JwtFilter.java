package com.backend.security;

import com.backend.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private Constants constants;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException, UsernameNotFoundException, JwtException
    {
        // Check if route not use authentication
        if(!doFilter(request)) {
            chain.doFilter(request,response);
            return;
        }

        final String token = request.getHeader("Authorization");

        if(token == null)
            throw new JwtException("Not found: Token!");

        if(!token.startsWith("Bearer "))
            throw new JwtException("Incorrect: Token");

        String jwt = token.substring(7);

        if(!jwtUtil.verify(jwt))
            throw new JwtException("Token invalid");

        Claims claims = jwtUtil.decode(jwt);

        UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());
        if(userDetails == null) throw new UsernameNotFoundException("Not found: User");

        //Set context
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        chain.doFilter(request,response);
    }

    private Boolean doFilter(HttpServletRequest request){
        String method = request.getMethod();
        String url = request.getRequestURI();
        return !constants.NO_AUTH_ROUTE.get(method).contains(url);
    }
}
