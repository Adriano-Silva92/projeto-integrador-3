package br.appLogin.appLogin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import java.time.LocalDateTime;

import br.appLogin.appLogin.model.Usuario;
import br.appLogin.appLogin.model.Role;
import br.appLogin.appLogin.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private UsuarioRepository ur;
    
    
    // RAIZ DO SISTEMA
    @GetMapping("/")
    public String raiz(HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        // SE JA ESTIVER LOGADO
        if (usuario != null) {
            return redirectPorRole(usuario.getRole());
        }
        // CASO NÃO ESTEJA
        return "redirect:/login";
    }

    // REDIRECIONAMENTO POR ROLE
    private String redirectPorRole(Role role) {
        return switch (role) {
            case ALUNO -> "redirect:/aluno/dashboard";
            case PROFESSOR -> "redirect:/turmas";
            case ADMIN -> "redirect:/admin";
        };
    }

    // TELA DE LOGIN
    @GetMapping("/login")
    public String login(HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario != null) {
            return redirectPorRole(usuario.getRole());
        }

        return "login";
    }

   
    // PROCESSAR LOGIN
    @PostMapping("/logar")
    public String loginUsuario(Usuario usuario, Model model, HttpSession session) {

        Usuario usuarioLogado = this.ur.login(usuario.getEmail(), usuario.getSenha());

        if (usuarioLogado != null) {
        	
            // USUÁRIO ONLINE
            usuarioLogado.setOnline(true);
            
            // DATA/HORA ÚLTIMO ACESSO
            usuarioLogado.setUltimoAcesso(LocalDateTime.now());
            
            // SALVA NO BANCO
            ur.save(usuarioLogado);
            
            // SALVA SESSÃO
            session.setAttribute("usuarioLogado", usuarioLogado);

            return redirectPorRole(usuarioLogado.getRole());
        }

        model.addAttribute("erro", "Email ou senha inválidos");
        return "login";
    }


    // LOGOUT
    @GetMapping("/logout")
    public String logout(HttpSession session) {    	
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        
        if (usuario != null) {
            //SALVA NO BANCO ONLINE = FALSE
            usuario.setOnline(false);

            ur.save(usuario);
        }
        session.invalidate();
        return "redirect:/login";
    }
}