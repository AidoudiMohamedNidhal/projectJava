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
            List<Utilisateur> admins = utilisateurRepository.findAllByUsername("admin");
            Utilisateur admin = admins.isEmpty() ? new Utilisateur() : admins.get(0);

            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);

            utilisateurRepository.save(admin);

            for (int i = 1; i < admins.size(); i++) {
                utilisateurRepository.delete(admins.get(i));
            }
        };
    }
}
