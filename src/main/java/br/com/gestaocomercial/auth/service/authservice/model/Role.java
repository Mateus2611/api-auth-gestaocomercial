package br.com.gestaocomercial.auth.service.authservice.model;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

import jakarta.annotation.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table (name = "tb_roles")
public class Role implements GrantedAuthority {
    
    public Role() {}


    public Role(Integer id, String name) {
        id = id;
        name = name;
    }



    public Role(String name) {
        this.name = name;
    }

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    public Integer getId() {
        return id;
    }


    public void setId(Integer id) {
        id = id;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        name = name;
    }

    @Override
    public @Nullable String getAuthority() {
        return this.name;
    }
}
