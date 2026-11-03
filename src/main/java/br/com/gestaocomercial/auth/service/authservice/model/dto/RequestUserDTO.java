package br.com.gestaocomercial.auth.service.authservice.model.dto;

import java.util.List;

public record RequestUserDTO(String name, boolean isActivate, List<String> roles) {
}
