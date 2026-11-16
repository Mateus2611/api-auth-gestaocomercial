package br.com.gestaocomercial.auth.service.authservice.infraestructure;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.gestaocomercial.auth.service.authservice.model.User;
import br.com.gestaocomercial.auth.service.authservice.repository.IUserRepository;
import br.com.gestaocomercial.auth.service.authservice.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component 
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired 
    private JwtService jwtService;

    @Autowired 
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
        throws ServletException, IOException {

        var token = this.recoverToken(request);

        if (token != null) {
            String email = jwtService.validateToken(token);
            User user = userRepository.findByEmail(email);

            if (user != null && Boolean.TRUE.equals(user.getIsActive())) {
                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        return authHeader.replace("Bearer ", "");
    }
}
