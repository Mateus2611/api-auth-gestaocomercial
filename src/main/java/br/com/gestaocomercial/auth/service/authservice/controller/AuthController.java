package br.com.gestaocomercial.auth.service.authservice.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.gestaocomercial.auth.service.authservice.model.Role;
import br.com.gestaocomercial.auth.service.authservice.model.User;
import br.com.gestaocomercial.auth.service.authservice.model.dto.LoginRequestDTO;
import br.com.gestaocomercial.auth.service.authservice.model.dto.RegisterRequestDTO;
import br.com.gestaocomercial.auth.service.authservice.model.dto.RequestUserDTO;
import br.com.gestaocomercial.auth.service.authservice.model.dto.TokenResponseDTO;
import br.com.gestaocomercial.auth.service.authservice.model.dto.UpdateRequestDTO;
import br.com.gestaocomercial.auth.service.authservice.repository.IRoleRepository;
import br.com.gestaocomercial.auth.service.authservice.repository.IUserRepository;
import br.com.gestaocomercial.auth.service.authservice.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private IUserRepository iUserRepository;

    @Autowired
    private IRoleRepository iRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping ("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody LoginRequestDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        var token = jwtService.generateToken((User) auth.getPrincipal());
        return ResponseEntity.ok(new TokenResponseDTO(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt_token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<Boolean> verificarUsuarioAtivo(@RequestParam String email) {
        User user = iUserRepository.findByEmail(email);
        boolean ativo = user != null && Boolean.TRUE.equals(user.getIsActive());
        return ResponseEntity.ok(ativo);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequestDTO data) {
        if (this.iUserRepository.findByEmail(data.email()) != null) {
            return ResponseEntity.badRequest().build();
        }

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User newUser = new User(data.email(), encryptedPassword);

        if (data.roles() != null && !data.roles().isEmpty()) {
            Set<String> nomesBuscados = new HashSet<>();
            for (String r : data.roles()) {
                String clean = r.trim().toLowerCase().replaceFirst("^role_", "");
                nomesBuscados.add(clean);
                nomesBuscados.add("role_" + clean);
            }

            Set<Role> userRoles = this.iRoleRepository.findAll().stream()
                    .filter(role -> nomesBuscados.contains(role.getName().toLowerCase()))
                    .collect(Collectors.toSet());

            newUser.setRoles(userRoles);
        }

        this.iUserRepository.save(newUser);
        return ResponseEntity.ok().build();
    }

    @Transactional
    @PutMapping("/users/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id, @RequestBody UpdateRequestDTO data) {
        Optional<User> userOptional = this.iUserRepository.findById(id);

        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();

        if (data.email() != null && !data.email().isBlank()) {
            var existingUser = this.iUserRepository.findByEmail(data.email());
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            user.setEmail(data.email());
        }

        if (data.password() != null && !data.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(data.password()));
        }

        if (data.roles() != null) {
        Set<String> nomesBuscados = new HashSet<>();
        for (String r : data.roles()) {
            String clean = r.trim().toLowerCase().replaceFirst("^role_", "");
            nomesBuscados.add(clean);
            nomesBuscados.add("role_" + clean);
        }

        Set<Role> updatedRoles = this.iRoleRepository.findAll().stream()
                .filter(role -> nomesBuscados.contains(role.getName().toLowerCase()))
                .collect(Collectors.toSet());

        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>(updatedRoles));
        } else {
            user.getRoles().clear();
            user.getRoles().addAll(updatedRoles);
        }
    }

    this.iUserRepository.saveAndFlush(user);

        this.iUserRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PatchMapping ("/users/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        Optional<User> userOptional = this.iUserRepository.findById(id);

        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        user.setIsActive(false);

        this.iUserRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PatchMapping ("/users/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        Optional<User> userOptional = this.iUserRepository.findById(id);

        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        user.setIsActive(true);

        this.iUserRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @GetMapping ("/users")
    public ResponseEntity<List<RequestUserDTO>> getUsers() {
        List<RequestUserDTO> users = this.iUserRepository.findAll().stream()
            .map(user -> {
                List<String> roleNames = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toList());

                return new RequestUserDTO(
                    user.getId(),
                    user.getEmail(),
                    user.getIsActive(),
                    roleNames
                );
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }
}
