package br.appLogin.appLogin.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;

import br.appLogin.appLogin.model.Usuario;
import br.appLogin.appLogin.model.Role;

public class AutenticacaoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession();
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            response.sendRedirect("/login");
            return false;
        }

        String uri = request.getRequestURI();
        Role role = usuario.getRole(); 

        System.out.println("URI: " + uri);
        System.out.println("Usuario: " + usuario);

        // PROTEGE /admin
        if (uri.startsWith("/admin")) {
            if (role != Role.ADMIN && role != Role.PROFESSOR) {
                response.sendRedirect("/dashboard");
                return false;
            }
        }

        // ALUNO NÃO ACESSA O DASHBOARD PRINCIPAL
        if (uri.startsWith("/dashboard")) {
            if (role == Role.ALUNO) {
                response.sendRedirect("/aluno/dashboard");
                return false;
            }
        }

        // PROTEGE ROTAS DE ALUNO
        if (uri.startsWith("/aluno")) {
            if (role != Role.ALUNO) {
                response.sendRedirect("/dashboard");
                return false;
            }
        }

        return true;
    }
}