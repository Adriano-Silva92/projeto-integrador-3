package br.appLogin.appLogin.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import br.appLogin.appLogin.model.Avaliacao;
import br.appLogin.appLogin.model.Professor;
import br.appLogin.appLogin.model.TipoAvaliacao;
import br.appLogin.appLogin.model.Turma;
import br.appLogin.appLogin.model.Usuario;
import br.appLogin.appLogin.repository.AvaliacaoRepository;
import br.appLogin.appLogin.repository.ProfessorRepository;
import br.appLogin.appLogin.repository.TurmaRepository;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    // 🔥 MÉTODO CENTRAL
    private Professor getProfessorLogado(HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            throw new RuntimeException("Usuário não logado");
        }

        return professorRepository
                .findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Professor não encontrado"));
    }

    // LISTAR AVALIAÇÕES DA TURMA
    @GetMapping("/turma/{turmaId}")
    public String listarPorTurma(@PathVariable Long turmaId,
                                Model model,
                                HttpSession session) {

        Professor professor = getProfessorLogado(session);

        Turma turma = turmaRepository.findById(turmaId).orElseThrow();

        // SEGURANÇA DE USUÁRIO PROFESSOR
        if (!turma.getProfessor().getId().equals(professor.getId())) {
            throw new RuntimeException("Acesso negado");
        }

        List<Avaliacao> avaliacoes = avaliacaoRepository.findByTurmaId(turmaId);

        model.addAttribute("turma", turma);
        model.addAttribute("avaliacoes", avaliacoes);

        return "avaliacoes"; 
    }

    // FORM NOVA AVALIAÇÃO
    @GetMapping("/nova/{turmaId}")
    public String novaAvaliacao(@PathVariable Long turmaId,
                               Model model,
                               HttpSession session) {

        Professor professor = getProfessorLogado(session);

        Turma turma = turmaRepository.findById(turmaId).orElseThrow();

        if (!turma.getProfessor().getId().equals(professor.getId())) {
            throw new RuntimeException("Acesso negado");
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setTurma(turma);

        model.addAttribute("avaliacao", avaliacao);
        model.addAttribute("turma", turma);

        return "avaliacao-form";
    }

    @PostMapping("/salvar")
    @ResponseBody
    public Map<String, Object> salvarAjax(
            @RequestParam String nome,
            @RequestParam String tipo,
            @RequestParam String dataVencimento,
            @RequestParam Long turmaId,
            HttpSession session) {

        //Professor professor = getProfessorLogado(session);

        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada"));

        Avaliacao a = new Avaliacao();

        a.setNome(nome);
        a.setTipo(TipoAvaliacao.valueOf(tipo));

        a.setDataCriacao(LocalDate.now());
        a.setDataVencimento(LocalDate.parse(dataVencimento));

        a.setTurma(turma);

        avaliacaoRepository.save(a);

        Map<String, Object> response = new HashMap<>();
        response.put("id", a.getId());
        response.put("nome", a.getNome());

        return response;
    }

    // EDITAR
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id,
                         Model model,
                         HttpSession session) {

        Professor professor = getProfessorLogado(session);

        Avaliacao avaliacao = avaliacaoRepository.findById(id).orElseThrow();

        if (!avaliacao.getTurma().getProfessor().getId().equals(professor.getId())) {
            throw new RuntimeException("Acesso negado");
        }

        model.addAttribute("avaliacao", avaliacao);
        model.addAttribute("turma", avaliacao.getTurma());

        return "avaliacao-form";
    }

    // EXCLUIR
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, HttpSession session) {

        Professor professor = getProfessorLogado(session);

        Avaliacao avaliacao = avaliacaoRepository.findById(id).orElseThrow();

        Long turmaId = avaliacao.getTurma().getId();

        if (!avaliacao.getTurma().getProfessor().getId().equals(professor.getId())) {
            throw new RuntimeException("Acesso negado");
        }

        avaliacaoRepository.deleteById(id);

        return "redirect:/avaliacoes/turma/" + turmaId;
    }
    
    // ENDPOINT PARA LISTAR AVALIAÇÕES PARA O ADMIN
    @GetMapping("/turma/{turmaId}/json")
    @ResponseBody
    public List<Map<String, Object>> listarAvaliacoesJson(
            @PathVariable Long turmaId,
            HttpSession session) {

        Professor professor = getProfessorLogado(session);

        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada"));

        if (!turma.getProfessor().getId().equals(professor.getId())) {
            throw new RuntimeException("Acesso negado");
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
}