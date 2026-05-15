package br.appLogin.appLogin.controller;

import java.time.LocalDate;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import br.appLogin.appLogin.model.*;
import br.appLogin.appLogin.repository.*;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/notas")
public class NotaController {

    @Autowired private NotaRepository notaRepository;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private AvaliacaoRepository avaliacaoRepository;
    @Autowired private TurmaRepository turmaRepository;
    @Autowired private ProfessorRepository professorRepository;
    @Autowired private AlunoTurmaRepository alunoTurmaRepository;

    // PROFESSOR LOGADO
    private Professor getProfessorLogado(HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            throw new RuntimeException("Usuário não logado");
        }

        return professorRepository
                .findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Professor não encontrado"));
    }


    // LISTAR
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("notas", notaRepository.findAll());
        return "notas";
    }


    // FORM NOVO
    @GetMapping("/novo")
    public String novaNota(Model model) {
        model.addAttribute("nota", new Nota());
        model.addAttribute("alunos", alunoRepository.findAll());
        model.addAttribute("avaliacoes", avaliacaoRepository.findAll());
        return "nota-form";
    }

  
    // SALVAR
    @PostMapping("/salvar")
    public String salvar(@RequestParam Long aluno,
                         @RequestParam Long avaliacao,
                         Nota nota) {

        Aluno alunoObj = alunoRepository.findById(aluno).orElseThrow();
        Avaliacao avaliacaoObj = avaliacaoRepository.findById(avaliacao).orElseThrow();

        nota.setAluno(alunoObj);
        nota.setAvaliacao(avaliacaoObj);

        notaRepository.save(nota);

        return "redirect:/dashboard";
    }

    // SALVAR AJAX
    @PostMapping("/salvar-ajax")
    @ResponseBody
    public Map<String, Object> salvarAjax(@RequestParam Long aluno,
                                          @RequestParam Long avaliacao,
                                          @RequestParam Double valor,
    	                                  @RequestParam(required = false) String dataLancamento) {

        Aluno alunoObj = alunoRepository.findById(aluno).orElseThrow();
        Avaliacao avaliacaoObj = avaliacaoRepository.findById(avaliacao).orElseThrow();

        // VERIFICA SE JÁ EXISTE
        Optional<Nota> existente = notaRepository
                .findByAlunoIdAndAvaliacaoId(aluno, avaliacao);

        Nota nota;

        if (existente.isPresent()) {
            // ATUALIZA
            nota = existente.get();
            nota.setValor(valor);

        } else {
            // INSERE
            nota = new Nota();
            nota.setAluno(alunoObj);
            nota.setAvaliacao(avaliacaoObj);
            nota.setValor(valor);
        }
        
        if (dataLancamento != null && !dataLancamento.isEmpty()) {
            nota.setDataLancamento(LocalDate.parse(dataLancamento));
        } else {
            nota.setDataLancamento(LocalDate.now());
        }

        notaRepository.save(nota);

        Map<String, Object> response = new HashMap<>();
        response.put("id", nota.getId());
        response.put("nome", alunoObj.getUsuario().getNome());
        response.put("ra", alunoObj.getRa());
        response.put("avaliacao", avaliacaoObj.getNome());
        response.put("valor", nota.getValor());

        return response;
    }

    // LANÇAR NOTA (
    @PostMapping("/lancar")
    @ResponseBody
    public ResponseEntity<?> lancarNota(@RequestBody NotaRequest request,
                                        HttpSession session) {

        try {
            Professor professor = getProfessorLogado(session);

            // VERIFICA TURMA
            Turma turma = turmaRepository.findById(request.getTurmaId())
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada"));

            if (!turma.getProfessor().getId().equals(professor.getId())) {
                throw new RuntimeException("Acesso negado");
            }

            // VERIFICA ALUNO DA TURMA
            boolean pertence = alunoTurmaRepository.findByTurmaId(turma.getId())
                    .stream()
                    .anyMatch(at -> at.getAluno().getId().equals(request.getAlunoId()));

            if (!pertence) {
                throw new RuntimeException("Aluno não pertence à turma");
            }

            // VERIFICA AVALIAÇÃO DA TURMA
            Avaliacao avaliacao = avaliacaoRepository.findById(request.getAvaliacaoId())
                    .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));

            if (!avaliacao.getTurma().getId().equals(turma.getId())) {
                throw new RuntimeException("Avaliação inválida");
            }

            // INSERE OU ATUALIZA
            Optional<Nota> existente = notaRepository
                    .findByAlunoIdAndAvaliacaoId(request.getAlunoId(), request.getAvaliacaoId());

            Nota nota = existente.orElse(new Nota());

            Aluno aluno = new Aluno();
            aluno.setId(request.getAlunoId());

            nota.setAluno(aluno);
            nota.setAvaliacao(avaliacao);
            nota.setValor(request.getValor());

            notaRepository.save(nota);

            return ResponseEntity.ok(Map.of("msg", "Nota salva"));

        } catch (RuntimeException ex) {
            return ResponseEntity
                    .status(403)
                    .body(Map.of("erro", ex.getMessage()));
        }
    }

    // LISTAR NOTAS POR TURMA + AVALIAÇÃO
    @GetMapping("/turma/{turmaId}/avaliacao/{avaliacaoId}")
    @ResponseBody
    public ResponseEntity<?> listarNotas(@PathVariable Long turmaId,
                                         @PathVariable Long avaliacaoId,
                                         HttpSession session) {

        try {
            Professor professor = getProfessorLogado(session);

            Turma turma = turmaRepository.findById(turmaId).orElseThrow();

            if (!turma.getProfessor().getId().equals(professor.getId())) {
                throw new RuntimeException("Acesso negado");
            }

            List<Nota> notas = notaRepository
                    .findByAvaliacaoId(avaliacaoId);

            List<NotaDTO> resposta = new ArrayList<>();

            for (Nota n : notas) {
                NotaDTO dto = new NotaDTO();
                dto.setAlunoId(n.getAluno().getId());
                dto.setValor(n.getValor());
                resposta.add(dto);
            }

            return ResponseEntity.ok(resposta);

        } catch (RuntimeException ex) {
            return ResponseEntity
                    .status(403)
                    .body(Map.of("erro", ex.getMessage()));
        }
    }

    // EXCLUIR
    @DeleteMapping("/excluir/{id}")
    @ResponseBody
    public String excluir(@PathVariable Long id) {
        notaRepository.deleteById(id);
        return "ok";
    }

    // DTOs
    public static class NotaRequest {
        private Long turmaId;
        private Long alunoId;
        private Long avaliacaoId;
        private double valor;

        public Long getTurmaId() { return turmaId; }
        public void setTurmaId(Long turmaId) { this.turmaId = turmaId; }

        public Long getAlunoId() { return alunoId; }
        public void setAlunoId(Long alunoId) { this.alunoId = alunoId; }

        public Long getAvaliacaoId() { return avaliacaoId; }
        public void setAvaliacaoId(Long avaliacaoId) { this.avaliacaoId = avaliacaoId; }

        public double getValor() { return valor; }
        public void setValor(double valor) { this.valor = valor; }
    }

    public static class NotaDTO {
        private Long alunoId;
        private double valor;

        public Long getAlunoId() { return alunoId; }
        public void setAlunoId(Long alunoId) { this.alunoId = alunoId; }

        public double getValor() { return valor; }
        public void setValor(double valor) { this.valor = valor; }
    }
    
    //ENDPOINT PARA LISTAR NOTAS 
    @GetMapping("/listar-ajax")
    @ResponseBody
    public List<Map<String, Object>> listarAjax() {

        List<Nota> notas = (List<Nota>) notaRepository.findAll();

        return notas.stream().map(nota -> {
            Map<String, Object> map = new HashMap<>();

            map.put("id", nota.getId());
            map.put("valor", nota.getValor());

            if (nota.getAluno() != null) {
                map.put("alunoNome", nota.getAluno().getUsuario().getNome());
                map.put("ra", nota.getAluno().getRa());
            }

            if (nota.getAvaliacao() != null) {
                map.put("avaliacao", nota.getAvaliacao().getNome());
            }

            map.put("dataLancamento", nota.getDataLancamento());

            return map;
        }).toList();
    }
}