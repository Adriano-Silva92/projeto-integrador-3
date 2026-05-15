package br.appLogin.appLogin.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;

import br.appLogin.appLogin.model.Usuario;
import br.appLogin.appLogin.model.Role;
import br.appLogin.appLogin.repository.UsuarioRepository;

// CRIA USUÁRIO ADMIN AUTO
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(UsuarioRepository usuarioRepository) {
        return args -> {

            if (usuarioRepository.count() == 0) {

                Usuario admin = new Usuario();
                admin.setNome("Admin");
                admin.setEmail("admin@admin.com");
                admin.setSenha("12345678");
                admin.setRole(Role.ADMIN);

                usuarioRepository.save(admin);

                System.out.println("ADMIN CRIADO COM SUCESSO!");
            }
        };
    }
}