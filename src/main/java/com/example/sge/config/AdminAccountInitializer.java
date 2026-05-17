package com.example.sge.config;

import com.example.sge.model.Role;
import com.example.sge.model.Utilisateur;
import com.example.sge.repository.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class AdminAccountInitializer {

    @Bean
    CommandLineRunner ensureAdminAccount(
            UtilisateurRepository utilisateurRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            ensureAccount(utilisateurRepository, passwordEncoder, "admin", "admin123", Role.ADMIN);
            ensureAccount(utilisateurRepository, passwordEncoder, "etudiant", "etudiant123", Role.USER);
        };
    }

    private void ensureAccount(
            UtilisateurRepository utilisateurRepository,
            PasswordEncoder passwordEncoder,
            String username,
            String password,
            Role role) {
        List<Utilisateur> users = utilisateurRepository.findAllByUsername(username);
        Utilisateur user = users.isEmpty() ? new Utilisateur() : users.get(0);

        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        utilisateurRepository.save(user);

        for (int i = 1; i < users.size(); i++) {
            utilisateurRepository.delete(users.get(i));
        }
    }
}
