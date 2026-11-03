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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @PostMapping ("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequestDTO data) {
        if (this.iUserRepository.findByEmail(data.email()) != null) {
            return ResponseEntity.badRequest().build();
        }

        String encryptedPassword = passwordEncoder.encode(data.password());
        User newUser = new User(data.email(), encryptedPassword);

        Set<Role> userRoles = new HashSet<>();
        for (String roleName : data.roles()) {
            Role role = iRoleRepository.findByName(roleName);

            if (role != null) {
                userRoles.add(role);
            }
        }
        newUser.setRoles(userRoles);

        this.iUserRepository.save(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping ("/users/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id, @RequestBody UpdateRequestDTO data) {
        Optional<User> userOptional = this.iUserRepository.findById(id);

        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();

        if (data.email() != null && !data.email().isBlank()) {
            var existingUser = this.iUserRepository.findByEmail(data.email());

            if (existingUser != null && existingUser.getUsername().equals(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            user.setEmail(data.email());
        }

        if (data.password() != null && !data.password().isBlank()) {
            String encryptedPassword = passwordEncoder.encode(data.password());
            user.setPassword(encryptedPassword);
        }

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

    @GetMapping ("/users")
    public ResponseEntity<List<RequestUserDTO>> getUsers() {
        List<RequestUserDTO> users = this.iUserRepository.findAll().stream()
            .map(user -> {
                List<String> roleNames = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toList());

                return new RequestUserDTO(
                    user.getEmail(),
                    user.getIsActive(),
                    roleNames
                );
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }
}
