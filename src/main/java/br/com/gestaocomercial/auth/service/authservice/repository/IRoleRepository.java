package br.com.gestaocomercial.auth.service.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.gestaocomercial.auth.service.authservice.model.Role;

public interface IRoleRepository extends JpaRepository<Role, Integer> {
    Role findByName(String name);
}
