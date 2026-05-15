package br.appLogin.appLogin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import br.appLogin.appLogin.model.Aluno;
import br.appLogin.appLogin.model.AlunoTurma;
import br.appLogin.appLogin.model.Professor;
import br.appLogin.appLogin.model.Turma;
import br.appLogin.appLogin.model.Usuario;
import br.appLogin.appLogin.model.Avaliacao;
import br.appLogin.appLogin.repository.AlunoRepository;
import br.appLogin.appLogin.repository.AlunoTurmaRepository;
import br.appLogin.appLogin.repository.AvaliacaoRepository;
import br.appLogin.appLogin.repository.FrequenciaRepository;
import br.appLogin.appLogin.repository.ProfessorRepository;
import br.appLogin.appLogin.repository.TurmaRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/turmas")
public class TurmaController {

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private AlunoTurmaRepository alunoTurmaRepository;

    @Autowired
    private AlunoRepository alunoRepository;
    
    @Autowired
    private AvaliacaoRepository avaliacaoRepository;
    
    @Autowired
    private FrequenciaRepository frequenciaRepository;

    //MÉTODO CENTRAL PARA PEGAR PROFESSOR LOGADO)
    private Professor getProfessorLogado(HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            throw new RuntimeException("Usuário não logado");
        }

        if (usuario.getRole().name().equals("ALUNO")) {
            throw new RuntimeException("Acesso negado");
        }

        return professorRepository
                .findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Professor não encontrado"));
    }

    //LISTAR TURMAS DO PROFESSOR
    @GetMapping
    public String listarTurmas(Model model, HttpSession session) {

        Professor professor = getProfessorLogado(session);

        List<Turma> turmas = turmaRepository.findByProfessorId(professor.getId());

        model.addAttribute("turmas", turmas);

        return "turmas";
    }

    //FORMULÁRIO NOVA TURMA
    @GetMapping("/nova")
    public String novaTurma(Model model) {
        model.addAttribute("turma", new Turma());
        return "turma-form";
    }

    @PostMapping("/salvar")
    @ResponseBody
    public Map<String, Object> salvarTurma(Turma turma, HttpSession session) {

        Professor professor = getProfessorLogado(session);

        turma.setProfessor(professor);

        turmaRepository.save(turma);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Turma criada com sucesso");

        return response;
    }

    //ENTRAR NA TURMA 
    @GetMapping("/{id}")
    public String abrirTurma(@PathVariable Long id, Model model, HttpSession session) {

        Professor professor = getProfessorLogado(session);

        Turma turma = turmaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
        

        // SEGURANÇA
        if (!turma.getProfessor().getId().equals(professor.getId())) {
            throw new RuntimeException("Acesso negado a esta turma");
        }
        // AVALIAÇÕES DA TURMA
        List<Avaliacao> avaliacoes = avaliacaoRepository.findByTurmaId(id);

        // ALUNOS DA TURMA
        List<AlunoTurma> alunosTurma = alunoTurmaRepository.findByTurmaId(id);

        //DTOs
        List<Map<String, Object>> alunosTurmaDTO = alunosTurma.stream()
        	    .map(at -> {
        	        Map<String, Object> map = new java.util.HashMap<>();
        	        map.put("id", at.getAluno().getId());
        	        map.put("alunoTurmaId", at.getId());
        	        map.put("nome", at.getAluno().getUsuario().getNome());
        	        map.put("ra", at.getAluno().getRa());
        	        map.put("email", at.getAluno().getUsuario().getEmail());
        	        return map;
        	    })
        	    .collect(java.util.stream.Collectors.toList());

        List<Aluno> alunos = (List<Aluno>) alunoRepository.findAll();

        model.addAttribute("turma", turma);
        model.addAttribute("alunosTurma", alunosTurmaDTO);
        model.addAttribute("alunos", alunos);
        model.addAttribute("avaliacoes", avaliacoes);

        return "turma-detalhe";
    }

 // EXCLUIR TURMA
    @Transactional
    @PostMapping("/excluir/{id}")
    @ResponseBody
    public Map<String, Object> excluirTurma(
            @PathVariable Long id,
            HttpSession session) {

        Professor professor = getProfessorLogado(session);

        Turma turma = turmaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada"));

        if (!turma.getProfessor().getId().equals(professor.getId())) {
            throw new RuntimeException("Acesso negado");
        }
        
        // REMOVE FREQUÊNCIAS
        frequenciaRepository.deleteByTurmaId(id);

        // REMOVE RELACIONAMENTOS
        alunoTurmaRepository.deleteByTurmaId(id);

        // REMOVE AVALIAÇÕES 
        avaliacaoRepository.deleteByTurmaId(id);
        

        // EXCLUI TURMA
        turmaRepository.deleteById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Turma excluída com sucesso");

        return response;
    }
    
    // ADICIONAR ALUNO
    @PostMapping("/{turmaId}/adicionar-aluno")
    public String adicionarAluno(
            @PathVariable Long turmaId,
            @RequestParam Long alunoId,
            HttpSession session) {

        Professor professor = getProfessorLogado(session);

        Turma turma = turmaRepository.findById(turmaId).orElseThrow();

        if (!turma.getProfessor().getId().equals(professor.getId())) {
            throw new RuntimeException("Acesso negado");
        }

        Aluno aluno = alunoRepository.findById(alunoId).orElseThrow();

        List<AlunoTurma> existentes = alunoTurmaRepository.findByTurmaId(turmaId);

        boolean jaExiste = existentes.stream()
                .anyMatch(at -> at.getAluno().getId().equals(alunoId));

        if (jaExiste) {
            return "redirect:/turmas/" + turmaId + "?erro=duplicado";
        }

        AlunoTurma at = new AlunoTurma();
        at.setAluno(aluno);
        at.setTurma(turma);

        alunoTurmaRepository.save(at);

        return "redirect:/turmas/" + turmaId;
    }

    // REMOVER ALUNO
    @PostMapping("/{turmaId}/remover-aluno/{alunoTurmaId}")
    public String removerAluno(
            @PathVariable Long turmaId,
            @PathVariable Long alunoTurmaId,
            HttpSession session) {

        Professor professor = getProfessorLogado(session);

        Turma turma = turmaRepository.findById(turmaId).orElseThrow();

        if (!turma.getProfessor().getId().equals(professor.getId())) {
            throw new RuntimeException("Acesso negado");
        }

        AlunoTurma at = alunoTurmaRepository.findById(alunoTurmaId)
                .orElseThrow();

        if (!at.getTurma().getId().equals(turmaId)) {
            throw new RuntimeException("Aluno não pertence a essa turma");
        }

        alunoTurmaRepository.delete(at);

        return "redirect:/turmas/" + turmaId;
    }
    
    // ENDPOINT PARA ADMIN
    @GetMapping("/{turmaId}/avaliacoes")
    @ResponseBody
    public List<Map<String, Object>> listarAvaliacoesJson(
            @PathVariable Long turmaId,
            HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada"));

        // ADMIN ACESSO TOTAL
        if (!usuario.getRole().name().equals("ADMIN")) {

            Professor professor = getProfessorLogado(session);

            if (!turma.getProfessor().getId().equals(professor.getId())) {
                throw new RuntimeException("Acesso negado");
            }
        }

        return avaliacaoRepository.findByTurmaId(turmaId)
            .stream()
            .map(a -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", a.getId());
                map.put("nome", a.getNome());
                return map;
            }).toList();
    }
    
    // LISTAR ALUNOS DA TURMA
    @GetMapping("/{turmaId}/alunos-ajax")
    @ResponseBody
    public List<Map<String, Object>> listarAlunosPorTurma(@PathVariable Long turmaId) {

        List<AlunoTurma> lista = alunoTurmaRepository.findByTurmaId(turmaId);

        return lista.stream().map(at -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", at.getAluno().getId());
            map.put("nome", at.getAluno().getUsuario().getNome());
            map.put("ra", at.getAluno().getRa());
            return map;
        }).toList();
    }
    
    
    @GetMapping("/listar-ajax")
    @ResponseBody
    public List<Map<String, Object>> listarTurmasAjax(HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        List<Turma> turmas;

        if (usuario.getRole().name().equals("ADMIN")) {
            turmas = (List<Turma>) turmaRepository.findAll();
        } else {
            Professor professor = getProfessorLogado(session);
            turmas = turmaRepository.findByProfessorId(professor.getId());
        }

        return turmas.stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("nome", t.getNome());
            return map;
        }).toList();
    }
}