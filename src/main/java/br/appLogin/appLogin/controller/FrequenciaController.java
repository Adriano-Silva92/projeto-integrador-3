package br.appLogin.appLogin.controller;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import br.appLogin.appLogin.model.*;
import br.appLogin.appLogin.repository.*;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/frequencia")
public class FrequenciaController {

    private final FrequenciaRepository frequenciaRepository;
    private final TurmaRepository turmaRepository;
    private final ProfessorRepository professorRepository;
    private final AlunoTurmaRepository alunoTurmaRepository;

    public FrequenciaController(FrequenciaRepository frequenciaRepository,
                                TurmaRepository turmaRepository,
                                ProfessorRepository professorRepository,
                                AlunoTurmaRepository alunoTurmaRepository) {
        this.frequenciaRepository = frequenciaRepository;
        this.turmaRepository = turmaRepository;
        this.professorRepository = professorRepository;
        this.alunoTurmaRepository = alunoTurmaRepository;
    }

    
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

    // SALVAR FREQUÊNCIA (EM LOTES)
    @PostMapping("/salvar")
    @ResponseBody
    public ResponseEntity<?> salvarFrequencia(@RequestBody FrequenciaRequest request,
                                              HttpSession session) {

        try {
            Professor professor = getProfessorLogado(session);

            Turma turma = turmaRepository.findById(request.getTurmaId())
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada"));

            // VALIDA PROFESSOR
            if (!turma.getProfessor().getId().equals(professor.getId())) {
                throw new RuntimeException("Acesso negado");
            }

            LocalDate data = request.getData();

            List<Frequencia> listaSalvar = new ArrayList<>();

            for (FrequenciaDTO dto : request.getFrequencias()) {

                // VALIDA SE ALUNO PERTENCE A TURMA
                boolean pertence = alunoTurmaRepository.findByTurmaId(turma.getId())
                        .stream()
                        .anyMatch(at -> at.getAluno().getId().equals(dto.getAlunoId()));

                if (!pertence) {
                    throw new RuntimeException("Aluno inválido");
                }

                // VERIFICA SE JÁ EXISTE FREQUÊNCIA OU ATUALIZA
                Optional<Frequencia> existente = frequenciaRepository
                        .findAll()
                        .stream()
                        .filter(f -> f.getAluno().getId().equals(dto.getAlunoId())
                                && f.getData().equals(data))
                        .findFirst();

                Frequencia frequencia = existente.orElse(new Frequencia());

                frequencia.setTurma(turma);
                frequencia.setData(data);

                Aluno aluno = new Aluno();
                aluno.setId(dto.getAlunoId());
                frequencia.setAluno(aluno);

                frequencia.setStatus(dto.getStatus());

                listaSalvar.add(frequencia);
            }

            frequenciaRepository.saveAll(listaSalvar);

            return ResponseEntity.ok(Map.of("msg", "Frequência salva com sucesso"));

        } catch (RuntimeException ex) {
            return ResponseEntity
                    .status(403)
                    .body(Map.of("erro", ex.getMessage()));
        }
    }

    // BUSCAR FREQUÊNCIA POR DATA
    @GetMapping("/turma/{turmaId}/data/{data}")
    @ResponseBody
    public ResponseEntity<?> getFrequencia(@PathVariable Long turmaId,
                                           @PathVariable String data,
                                           HttpSession session) {

        try {
            Professor professor = getProfessorLogado(session);

            Turma turma = turmaRepository.findById(turmaId)
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada"));

            if (!turma.getProfessor().getId().equals(professor.getId())) {
                throw new RuntimeException("Acesso negado");
            }

            LocalDate dataFormatada = LocalDate.parse(data);

            List<Frequencia> lista = frequenciaRepository
                    .findByTurmaIdAndData(turmaId, dataFormatada);

            List<FrequenciaDTO> resposta = lista.stream().map(f -> {
                FrequenciaDTO dto = new FrequenciaDTO();
                dto.setAlunoId(f.getAluno().getId());
                dto.setStatus(f.getStatus());
                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(resposta);

        } catch (RuntimeException ex) {
            return ResponseEntity
                    .status(403)
                    .body(Map.of("erro", ex.getMessage()));
        }
    }
    
    // BUSCAR FALTAS DO ALUNO
    @GetMapping("/aluno/{alunoId}/turma/{turmaId}/faltas")
    @ResponseBody
    public ResponseEntity<?> buscarFaltasAluno(@PathVariable Long alunoId,
                                               @PathVariable Long turmaId,
                                               HttpSession session) {

         try {
             Professor professor = getProfessorLogado(session);

             Turma turma = turmaRepository.findById(turmaId)
                     .orElseThrow(() -> new RuntimeException("Turma não encontrada"));

             // VALIDA PROFESSOR
             if (!turma.getProfessor().getId().equals(professor.getId())) {
                 throw new RuntimeException("Acesso negado");
            }

             // VALIDA ALUNO 
             boolean pertence = alunoTurmaRepository.findByTurmaId(turmaId)
                     .stream()
                    .anyMatch(at -> at.getAluno().getId().equals(alunoId));

             if (!pertence) {
                 throw new RuntimeException("Aluno não pertence à turma");
             }

             // BUSCAR FALTAS
             List<Frequencia> faltas = frequenciaRepository.findAll()
                     .stream()
                     .filter(f -> f.getAluno().getId().equals(alunoId)
                             && f.getTurma().getId().equals(turmaId)
                             && f.getStatus() == Frequencia.StatusFrequencia.FALTA)
                     .sorted(Comparator.comparing(Frequencia::getData).reversed())
                     .toList();

              // RESPOSTA JSON
             List<Map<String, Object>> resposta = faltas.stream().map(f -> {
                 Map<String, Object> map = new HashMap<>();
                 map.put("data", f.getData());
                 map.put("observacao", "-"); 
                 return map;
             }).toList();

             return ResponseEntity.ok(resposta);

         } catch (RuntimeException ex) {
             return ResponseEntity
                     .status(403)
                     .body(Map.of("erro", ex.getMessage()));
         }
     }

    // DTOs
    public static class FrequenciaRequest {
        private Long turmaId;
        private LocalDate data;
        private List<FrequenciaDTO> frequencias;

        public Long getTurmaId() { return turmaId; }
        public void setTurmaId(Long turmaId) { this.turmaId = turmaId; }

        public LocalDate getData() { return data; }
        public void setData(LocalDate data) { this.data = data; }

        public List<FrequenciaDTO> getFrequencias() { return frequencias; }
        public void setFrequencias(List<FrequenciaDTO> frequencias) { this.frequencias = frequencias; }
    }

    public static class FrequenciaDTO {
        private Long alunoId;
        private Frequencia.StatusFrequencia status;

        public Long getAlunoId() { return alunoId; }
        public void setAlunoId(Long alunoId) { this.alunoId = alunoId; }

        public Frequencia.StatusFrequencia getStatus() { return status; }
        public void setStatus(Frequencia.StatusFrequencia status) { this.status = status; }
    }
}