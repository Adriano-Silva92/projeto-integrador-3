package br.appLogin.appLogin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import br.appLogin.appLogin.model.Aluno;
import br.appLogin.appLogin.model.Nota;
import br.appLogin.appLogin.model.Usuario;
import br.appLogin.appLogin.model.Role;
import br.appLogin.appLogin.repository.AlunoRepository;
import br.appLogin.appLogin.repository.NotaRepository;

import jakarta.servlet.http.HttpSession;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/aluno")
public class AlunoDashboardController {

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private NotaRepository notaRepository;

    // 🔥 MÉTODO CENTRAL (corrigido)
    private Aluno getAlunoLogado(HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            throw new RuntimeException("Usuário não logado");
        }

        if (usuario.getRole() != Role.ALUNO) {
            throw new RuntimeException("Acesso negado - não é aluno");
        }

        return alunoRepository.findByUsuarioEmail(usuario.getEmail())
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
    }

    // DASHBOARD
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {

        try {
            Aluno aluno = getAlunoLogado(session);
            model.addAttribute("aluno", aluno);
            return "dashboard-aluno";
        } catch (Exception e) {
            e.printStackTrace();
            return "erro";
        }
    }

    // NOTAS
    @GetMapping("/notas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getNotasAluno(HttpSession session) {

        try {
            Aluno aluno = getAlunoLogado(session);

            List<Nota> notas = notaRepository.findByAlunoId(aluno.getId());

            List<Map<String, Object>> notasResponse = notas.stream().map(nota -> {
                Map<String, Object> notaMap = new HashMap<>();
                notaMap.put("id", nota.getId());
                notaMap.put("valor", nota.getValor());

                notaMap.put("dataLancamento",
                        nota.getDataLancamento() != null
                                ? nota.getDataLancamento().toString()
                                : "");

               
                notaMap.put("avaliacao", nota.getAvaliacao().getNome());
                notaMap.put("tipo", nota.getAvaliacao().getTipo().toString());

                return notaMap;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("notas", notasResponse);
            response.put("aluno", Map.of(
                    "id", aluno.getId(),
                    "nome", aluno.getUsuario().getNome(),
                    "ra", aluno.getRa(),
                    "email", aluno.getUsuario().getEmail()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).build();
        }
    }

    // BOLETIM
    @GetMapping("/boletim")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBoletim(HttpSession session) {

        try {
            Aluno aluno = getAlunoLogado(session);

            List<Nota> notas = notaRepository.findByAlunoId(aluno.getId());

            Map<String, List<Double>> notasPorAvaliacao = new HashMap<>();

            for (Nota nota : notas) {

                String chave = nota.getAvaliacao().getNome();

                notasPorAvaliacao
                        .computeIfAbsent(chave, k -> new ArrayList<>())
                        .add(nota.getValor());
            }

            List<Map<String, Object>> medias = new ArrayList<>();

            double somaTotal = 0;
            int totalNotas = 0;

            for (Map.Entry<String, List<Double>> entry : notasPorAvaliacao.entrySet()) {

                double media = entry.getValue().stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0);

                somaTotal += entry.getValue().stream().mapToDouble(Double::doubleValue).sum();
                totalNotas += entry.getValue().size();

                Map<String, Object> item = new HashMap<>();
                item.put("avaliacao", entry.getKey());
                item.put("media", media);
                item.put("notas", entry.getValue());

                String situacao;
                if (media >= 6) {
                    situacao = "Aprovado";
                } else if (media >= 5) {
                    situacao = "Recuperação";
                } else {
                    situacao = "Reprovado";
                }

                item.put("situacao", situacao);

                medias.add(item);
            }

            double mediaGeral = totalNotas > 0 ? somaTotal / totalNotas : 0;

            Map<String, Object> response = new HashMap<>();
            response.put("aluno", Map.of(
                    "id", aluno.getId(),
                    "nome", aluno.getUsuario().getNome(),
                    "ra", aluno.getRa(),
                    "email", aluno.getUsuario().getEmail()
            ));
            response.put("avaliacoes", medias);
            response.put("mediaGeral", mediaGeral);
            response.put("anoLetivo", 2024);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).build();
        }
    }
}