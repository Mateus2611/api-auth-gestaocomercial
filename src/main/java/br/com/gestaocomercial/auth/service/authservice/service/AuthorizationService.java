package br.com.gestaocomercial.auth.service.authservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.gestaocomercial.auth.service.authservice.repository.IUserRepository;

@Service 
public class AuthorizationService implements UserDetailsService {

    @Autowired 
    private IUserRepository _userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return _userRepository.findByEmail(username);
    }
}
