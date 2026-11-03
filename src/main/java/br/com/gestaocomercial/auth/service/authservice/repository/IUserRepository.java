package br.com.gestaocomercial.auth.service.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.gestaocomercial.auth.service.authservice.model.User;

public interface IUserRepository extends JpaRepository<User, Long> {
    
    UserDetails findByEmail(String email);
}
