package br.appLogin.appLogin.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import br.appLogin.appLogin.model.*;
import br.appLogin.appLogin.repository.*;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping
public class DashboardController {

    private final AlunoRepository alunoRepository;
    private final NotaRepository notaRepository;
    private final TurmaRepository turmaRepository;
    private final AlunoTurmaRepository alunoTurmaRepository;
    private final ProfessorRepository professorRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final FrequenciaRepository frequenciaRepository;

    public DashboardController(AlunoRepository alunoRepository,
                               NotaRepository notaRepository,
                               TurmaRepository turmaRepository,
                               AlunoTurmaRepository alunoTurmaRepository,
                               ProfessorRepository professorRepository,
                               AvaliacaoRepository avaliacaoRepository,
                               FrequenciaRepository frequenciaRepository) {
        this.alunoRepository = alunoRepository;
        this.notaRepository = notaRepository;
        this.turmaRepository = turmaRepository;
        this.alunoTurmaRepository = alunoTurmaRepository;
        this.professorRepository = professorRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.frequenciaRepository = frequenciaRepository;
    }

    // VERIFICA PROFESSOR LOGADO 
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

    
    // DASHBOARD POR TURMA 
    @GetMapping("/dashboard/{turmaId}")
    public String dashboard(@PathVariable Long turmaId,
                            Model model,
                            HttpSession session) {

        try {
            
            // PROFESSOR LOGADO
            Professor professor = getProfessorLogado(session);

            
            // BUSCA TURMA
            Turma turma = turmaRepository.findById(turmaId)
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada"));

            
            // SEGURANÇA
            if (!turma.getProfessor().getId().equals(professor.getId())) {
                throw new RuntimeException("Acesso negado a esta turma");
            }
                         
            // BUSCA AVALIAÇÕES
            List<Avaliacao> avaliacoes = avaliacaoRepository.findByTurmaId(turmaId);
            
            
            // BUSCA ALUNOS
            List<AlunoTurma> alunosTurma = alunoTurmaRepository.findByTurmaId(turmaId);

            List<Aluno> alunos = alunosTurma.stream()
                    .map(AlunoTurma::getAluno)
                    .collect(Collectors.toList());

            int total = alunos.size();

            double mediaGeral = alunos.stream()
                    .mapToDouble(Aluno::mediaNotas)
                    .average()
                    .orElse(0.0);

            long aprovados = alunos.stream()
                    .filter(a -> "Aprovado".equals(a.getSituacao()))
                    .count();

            long reprovados = alunos.stream()
                    .filter(a -> "Reprovado".equals(a.getSituacao()))
                    .count();
            
            LocalDate hoje =
                    LocalDate.now(
                            ZoneId.of("America/Sao_Paulo")
                    );
            
            long totalFaltas =
                    frequenciaRepository.countByTurmaIdAndStatus(
                            turmaId,
                            Frequencia.StatusFrequencia.FALTA
                    );

            long presentesHoje =
                    frequenciaRepository.countByTurmaIdAndStatusAndData(
                            turmaId,
                            Frequencia.StatusFrequencia.PRESENTE,
                            hoje
                    );

            long faltasHoje =
                    frequenciaRepository.countByTurmaIdAndStatusAndData(
                            turmaId,
                            Frequencia.StatusFrequencia.FALTA,
                            hoje
                    );

            long totalPresencas =
                    frequenciaRepository.countByTurmaIdAndStatus(
                            turmaId,
                            Frequencia.StatusFrequencia.PRESENTE
                    );

            double frequenciaTotal = 0.0;

            long totalRegistros =
                    totalPresencas + totalFaltas;

            if (totalRegistros > 0) {

                frequenciaTotal =
                        ((double) totalPresencas / totalRegistros)
                                * 100;
            }

            // ENVIA PARA VIEW
            model.addAttribute("turma", turma);
            model.addAttribute("alunos", alunos);
            model.addAttribute("totalAlunos", total);
            model.addAttribute("mediaGeral", mediaGeral);
            model.addAttribute("aprovados", aprovados);
            model.addAttribute("reprovados", reprovados);
            model.addAttribute("avaliacoes", avaliacoes);
            model.addAttribute("totalFaltas", totalFaltas);
            model.addAttribute("presentesHoje", presentesHoje);
            model.addAttribute("faltasHoje", faltasHoje);
            model.addAttribute("frequenciaTotal", frequenciaTotal);

            model.addAttribute("erro", null); 
            return "dashboard";

        } catch (RuntimeException ex) {

            
            // TRATAMENTO DE ERRO 
            model.addAttribute("erro", ex.getMessage());
            model.addAttribute("turma", null);
            model.addAttribute("alunos", new ArrayList<>());
            model.addAttribute("totalAlunos", 0);
            model.addAttribute("mediaGeral", 0.0);
            model.addAttribute("aprovados", 0);
            model.addAttribute("reprovados", 0);

            return "dashboard";
        }
    }

    
    // JSON DO DASHBOARD
    @ResponseBody
    @GetMapping("/dashboard/{turmaId}/dados")
    public ResponseEntity<?> getDashboardData(@PathVariable Long turmaId,
                                              HttpSession session) {
        try {

            Professor professor = getProfessorLogado(session);

            Turma turma = turmaRepository.findById(turmaId)
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada"));

            if (!turma.getProfessor().getId().equals(professor.getId())) {
                throw new RuntimeException("Acesso negado");
            }

            List<AlunoTurma> alunosTurma = alunoTurmaRepository.findByTurmaId(turmaId);

            List<AlunoDTO> alunosDTO = new ArrayList<>();

            for (AlunoTurma at : alunosTurma) {

                Aluno aluno = at.getAluno();

                AlunoDTO dto = new AlunoDTO();
                dto.setId(aluno.getId());
                dto.setRa(aluno.getRa());
                dto.setNome(aluno.getUsuario().getNome());
                dto.setEmail(aluno.getUsuario().getEmail());
             // BUSCA NOTAS DA TURMA
                List<Nota> notas = notaRepository
                    .findByAlunoIdAndAvaliacaoTurmaId(aluno.getId(), turmaId);

                // CALCULA MÉDIA CORRETA
                double media = notas.stream()
                    .mapToDouble(Nota::getValor)
                    .average()
                    .orElse(0.0);

                dto.setMediaNotas(media);

                // SITUAÇÃO BASEADA NA MÉDIA DA TURMA
                dto.setSituacao(media >= 5 ? "Aprovado" : "Reprovado");

                alunosDTO.add(dto);
            }

            DashboardData data = new DashboardData();
            data.setAlunos(alunosDTO);
            data.setTotalAlunos(alunosDTO.size());
            data.setMediaGeral(
                    alunosDTO.stream()
                            .mapToDouble(AlunoDTO::getMediaNotas)
                            .average()
                            .orElse(0.0)
            );
            data.setAprovados(
                    alunosDTO.stream()
                            .filter(a -> "Aprovado".equals(a.getSituacao()))
                            .count()
            );
            data.setReprovados(
                    alunosDTO.stream()
                            .filter(a -> "Reprovado".equals(a.getSituacao()))
                            .count()
            );

            return ResponseEntity.ok(data);

        } catch (RuntimeException ex) {

        	return ResponseEntity
        	        .status(403)
        	        .body(Map.of("erro", ex.getMessage()));
        }
    }


 // DETALHES DO ALUNO (COM SEGURANÇA)
 @GetMapping("/dashboard/{turmaId}/aluno/{id}")
 @ResponseBody
 public AlunoDetalhesDTO getAlunoDetalhes(@PathVariable Long turmaId,
                                           @PathVariable Long id,
                                           HttpSession session) {

     Professor professor = getProfessorLogado(session);

     Turma turma = turmaRepository.findById(turmaId)
             .orElseThrow(() -> new RuntimeException("Turma não encontrada"));

     // PROFESSOR LOGADO
     if (!turma.getProfessor().getId().equals(professor.getId())) {
         throw new RuntimeException("Acesso negado");
     }

     // GARANTE QUE ALUNO PERTENCE A TURMA 
     boolean pertence = alunoTurmaRepository.findByTurmaId(turmaId)
             .stream()
             .anyMatch(at -> at.getAluno().getId().equals(id));

     if (!pertence) {
         throw new RuntimeException("Aluno não pertence a esta turma");
     }

     Aluno aluno = alunoRepository.findById(id)
             .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));

     AlunoDetalhesDTO detalhes = new AlunoDetalhesDTO();

     detalhes.setId(aluno.getId());
     detalhes.setRa(aluno.getRa());
     detalhes.setNome(aluno.getUsuario().getNome());
     detalhes.setEmail(aluno.getUsuario().getEmail());

     // BUSCA NOTAS FILTRADAS PELA TURMA
     List<Nota> notas = notaRepository
             .findByAlunoIdAndAvaliacaoTurmaId(aluno.getId(), turmaId);

     // CALCULA MÉDIA CORRETA (SÓ DESSA TURMA)
     double media = notas.stream()
             .mapToDouble(Nota::getValor)
             .average()
             .orElse(0.0);

     detalhes.setMediaNotas(media);

     detalhes.setSituacao(media >= 5 ? "Aprovado" : "Reprovado");

     // MONTA LISTA DE NOTAS
     List<NotaDTO> notasDTO = new ArrayList<>();

     for (Nota nota : notas) {
         NotaDTO dto = new NotaDTO();
         dto.setAvaliacao(nota.getAvaliacao().getNome());
         dto.setTipo(nota.getAvaliacao().getTipo().toString());
         dto.setValor(nota.getValor());
         notasDTO.add(dto);
     }

     detalhes.setNotas(notasDTO);

     return detalhes;
 }
  
    // DTOs
    static class DashboardData {
        private List<AlunoDTO> alunos;
        private int totalAlunos;
        private double mediaGeral;
        private long aprovados;
        private long reprovados;

        public List<AlunoDTO> getAlunos() { return alunos; }
        public void setAlunos(List<AlunoDTO> alunos) { this.alunos = alunos; }

        public int getTotalAlunos() { return totalAlunos; }
        public void setTotalAlunos(int totalAlunos) { this.totalAlunos = totalAlunos; }

        public double getMediaGeral() { return mediaGeral; }
        public void setMediaGeral(double mediaGeral) { this.mediaGeral = mediaGeral; }

        public long getAprovados() { return aprovados; }
        public void setAprovados(long aprovados) { this.aprovados = aprovados; }

        public long getReprovados() { return reprovados; }
        public void setReprovados(long reprovados) { this.reprovados = reprovados; }
    }

    static class AlunoDTO {
        private Long id;
        private String ra;
        private String nome;
        private String email;
        private double mediaNotas;
        private String situacao;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getRa() { return ra; }
        public void setRa(String ra) { this.ra = ra; }

        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public double getMediaNotas() { return mediaNotas; }
        public void setMediaNotas(double mediaNotas) { this.mediaNotas = mediaNotas; }

        public String getSituacao() { return situacao; }
        public void setSituacao(String situacao) { this.situacao = situacao; }
    }

    static class AlunoDetalhesDTO {
        private Long id;
        private String ra;
        private String nome;
        private String email;
        private double mediaNotas;
        private String situacao;
        private List<NotaDTO> notas;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getRa() { return ra; }
        public void setRa(String ra) { this.ra = ra; }

        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public double getMediaNotas() { return mediaNotas; }
        public void setMediaNotas(double mediaNotas) { this.mediaNotas = mediaNotas; }

        public String getSituacao() { return situacao; }
        public void setSituacao(String situacao) { this.situacao = situacao; }

        public List<NotaDTO> getNotas() { return notas; }
        public void setNotas(List<NotaDTO> notas) { this.notas = notas; }
    }

    static class NotaDTO {
        private String avaliacao;
        private String tipo;
        private double valor;

        public String getAvaliacao() { return avaliacao; }
        public void setAvaliacao(String avaliacao) { this.avaliacao = avaliacao; }

        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }

        public double getValor() { return valor; }
        public void setValor(double valor) { this.valor = valor; }
    }
}