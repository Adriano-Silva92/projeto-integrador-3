package br.appLogin.appLogin.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import br.appLogin.appLogin.repository.AlunoRepository;
import br.appLogin.appLogin.repository.UsuarioRepository;
import br.appLogin.appLogin.repository.NotaRepository;

@Controller
public class AdminController {

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotaRepository notaRepository;

    @GetMapping("/admin")
    public String adminHome(Model model) {

        model.addAttribute("alunos", alunoRepository.findAll());
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("notas", notaRepository.findAll());

        model.addAttribute("totalAlunos", alunoRepository.count());
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        model.addAttribute("totalNotas", notaRepository.count());

        return "admin";
    }
    
    @GetMapping("/admin/dashboard-data")
    @ResponseBody
    public Map<String, Object> getDashboardData() {

        Map<String, Object> data = new HashMap<>();

        data.put("totalAlunos", alunoRepository.count());
        data.put("totalUsuarios", usuarioRepository.count());
        data.put("totalNotas", notaRepository.count());

        return data;
    }
}