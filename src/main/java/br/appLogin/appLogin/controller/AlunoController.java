package br.appLogin.appLogin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; 
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import br.appLogin.appLogin.model.Aluno;
//import br.appLogin.appLogin.model.Nota;
import br.appLogin.appLogin.model.Usuario;
import br.appLogin.appLogin.repository.AlunoRepository;
import br.appLogin.appLogin.repository.AlunoTurmaRepository;
import br.appLogin.appLogin.repository.NotaRepository;
import br.appLogin.appLogin.repository.UsuarioRepository;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/alunos")
public class AlunoController {

    @Autowired
    private AlunoRepository alunoRepository;
    
    @Autowired
    private AlunoTurmaRepository alunoTurmaRepository;
    
    @Autowired
    private NotaRepository notaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("alunos", alunoRepository.findAll());
        return "alunos";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("aluno", new Aluno());
        return "aluno-form";
    }

    @PostMapping("/salvar")
    public String salvar(Aluno aluno) {
        alunoRepository.save(aluno);
        return "redirect:/admin";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("aluno", alunoRepository.findById(id).orElseThrow());
        return "aluno-form";
    }

    @DeleteMapping("/excluir/{id}")
    @Transactional
    public ResponseEntity<?> excluir(@PathVariable Long id) {

        Aluno aluno = alunoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));

        // REMOVE VÍNCULOS COM TURMA
        alunoTurmaRepository.deleteByAlunoId(id);

        // REMOVE NOTA
        notaRepository.deleteAll(notaRepository.findByAlunoId(id));

        // GUARDA USUÁRIO
        Usuario usuario = aluno.getUsuario();

        // REMOVE ALUNO
        alunoRepository.delete(aluno);

        // REMOVE USUÁRIO 
        if (usuario != null) {
            usuarioRepository.delete(usuario);
        }

        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/salvar-ajax")
    @ResponseBody
    @Transactional
    public Map<String, Object> salvarAjax(Aluno aluno) {

        Usuario usuario = aluno.getUsuario();

        if (usuario != null) {

            // DEFINE PERFIL 
            usuario.setRole(br.appLogin.appLogin.model.Role.ALUNO);

            // USA SENHA PADRÃO SE NÃO INSERIR
            if (usuario.getSenha() == null || usuario.getSenha().isEmpty()) {
                usuario.setSenha("123456");
            }

            usuarioRepository.save(usuario);
        }

        alunoRepository.save(aluno);

        Map<String, Object> response = new HashMap<>();
        response.put("id", aluno.getId());
        response.put("ra", aluno.getRa());
        response.put("nome", usuario.getNome());
        response.put("email", usuario.getEmail());

        return response;
    }
    
    @GetMapping("/listar-ajax")
    @ResponseBody
    public List<Map<String, Object>> listarAjax() {

        List<Aluno> alunos = (List<Aluno>) alunoRepository.findAll();

        return alunos.stream().map(aluno -> {
            Map<String, Object> map = new HashMap<>();

            map.put("id", aluno.getId());
            map.put("ra", aluno.getRa());

            if (aluno.getUsuario() != null) {
                map.put("nome", aluno.getUsuario().getNome());
                map.put("email", aluno.getUsuario().getEmail());
            } else {
                map.put("nome", "Sem usuário");
                map.put("email", "-");
            }

            map.put("media", aluno.mediaNotas());
            map.put("situacao", aluno.getSituacao());

            return map;
        }).toList();
    }
    
    @PutMapping("/editar-ajax/{id}")
    @ResponseBody
    @Transactional
    public Map<String, Object> editarAjax(
            @PathVariable Long id,
            Aluno alunoForm // vem do FormData
    ) {

        Aluno aluno = alunoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));

        // ATUALIZA O RA
        aluno.setRa(alunoForm.getRa());

        // ATUALIZA USUÁRIO
        Usuario usuario = aluno.getUsuario();

        if (usuario != null) {
            usuario.setNome(alunoForm.getUsuario().getNome());
            usuario.setEmail(alunoForm.getUsuario().getEmail());

            // ATUALIZA SENHAR SE ALTERADA
            if (alunoForm.getUsuario().getSenha() != null &&
                !alunoForm.getUsuario().getSenha().isEmpty()) {

                usuario.setSenha(alunoForm.getUsuario().getSenha());
            }

            usuarioRepository.save(usuario);
        }

        alunoRepository.save(aluno);

        
        Map<String, Object> response = new HashMap<>();
        response.put("id", aluno.getId());
        response.put("ra", aluno.getRa());
        response.put("nome", usuario != null ? usuario.getNome() : "");
        response.put("email", usuario != null ? usuario.getEmail() : "");

        return response;
    }
}