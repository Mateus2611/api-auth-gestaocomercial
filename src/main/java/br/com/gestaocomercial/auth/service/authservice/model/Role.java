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
        Id = id;
        Name = name;
    }



    public Role(String name) {
        this.Name = name;
    }

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer Id;
    @Column(name = "name", nullable = false, unique = true)
    private String Name;

    public Integer getId() {
        return Id;
    }


    public void setId(Integer id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }


    public void setName(String name) {
        Name = name;
    }

    @Override
    public @Nullable String getAuthority() {
        return this.Name;
    }
}
