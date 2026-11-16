package br.com.gestaocomercial.auth.service.authservice.model.dto;

import java.util.List;

public record UpdateRequestDTO(
    String email,
    String password,
    List<String> roles
) {}