package br.com.gestaocomercial.auth.service.authservice.model.dto;

import java.util.List;

public record RequestUserDTO(Long id, String name, boolean isActivate, List<String> roles) {
}
