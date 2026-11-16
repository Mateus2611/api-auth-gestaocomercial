package br.com.gestaocomercial.auth.service.authservice.repository;

import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.gestaocomercial.auth.service.authservice.model.Role;

public interface IRoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
    Set<Role> findByNameIn(List<String> names);
    @Query("SELECT r FROM Role r WHERE LOWER(r.name) IN :names")
    Set<Role> findByNameInIgnoreCase(@Param("names") List<String> names);
}