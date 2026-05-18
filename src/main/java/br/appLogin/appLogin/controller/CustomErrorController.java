package br.appLogin.appLogin.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        
        // Pega o status code do erro
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Integer statusCode = status != null ? Integer.valueOf(status.toString()) : 500;
        
        // Configura variáveis baseadas no status code
        String icon = "❌";
        String errorTitle = "Erro no Sistema";
        String message = "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.";
        
        switch (statusCode) {
            case 400:
                icon = "❓";
                errorTitle = "Requisição Inválida";
                message = "A requisição enviada é inválida. Verifique os dados e tente novamente.";
                break;
                
            case 403:
                icon = "⛔";
                errorTitle = "Acesso Proibido";
                message = "Você não tem permissão para acessar esta página.";
                break;
                
            case 404:
                icon = "🔍";
                errorTitle = "Página Não Encontrada";
                message = "A página que você está procurando não existe ou foi removida.";
                break;
                
            case 500:
                icon = "⚠️";
                errorTitle = "Erro Interno do Servidor";
                message = "Ocorreu um erro interno no servidor. Nossa equipe já foi notificada.";
                break;
                
            default:
                icon = "❌";
                errorTitle = "Erro " + statusCode;
                message = "Ocorreu um erro inesperado. Tente novamente mais tarde.";
                break;
        }
        
        // Adiciona atributos ao modelo
        model.addAttribute("statusCode", statusCode);
        model.addAttribute("icon", icon);
        model.addAttribute("errorTitle", errorTitle);
        model.addAttribute("message", message);
        
        // Opcional: adicionar detalhes do erro em ambiente de desenvolvimento
        Object errorMessage = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        if (errorMessage != null && !errorMessage.toString().isEmpty()) {
            model.addAttribute("errorDetails", errorMessage.toString());
        }
        
        return "erro"; // Retorna a página erro.html
    }
}