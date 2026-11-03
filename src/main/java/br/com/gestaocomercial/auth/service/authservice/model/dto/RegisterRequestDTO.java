package br.com.gestaocomercial.auth.service.authservice.model.dto;

import java.util.Set;

public record RegisterRequestDTO(String email, String password, Set<String> roles) {

}
