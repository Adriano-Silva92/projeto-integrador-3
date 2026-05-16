package br.appLogin.appLogin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.cloudinary.Cloudinary;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.utils.ObjectUtils;
import java.util.Map;

import br.appLogin.appLogin.model.Aluno;
import br.appLogin.appLogin.model.Professor;
import br.appLogin.appLogin.model.Usuario;
import br.appLogin.appLogin.repository.AlunoRepository;
import br.appLogin.appLogin.repository.ProfessorRepository;
import br.appLogin.appLogin.repository.UsuarioRepository;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private ProfessorRepository professorRepository;
    
    @Autowired
    private Cloudinary cloudinary;
    

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "usuarios";
    }
    
    // NOVO USUÁRIO
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "admin";
    }
    
    // SALVAR USUÁRIO
    @PostMapping("/salvar")
    public String salvar(Usuario usuario) {
        usuarioRepository.save(usuario);
        return "redirect:/admin";
    }
    
    // EXCLUIR USUÁRIO
    @DeleteMapping("/excluir/{id}")
    @ResponseBody
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        try {
            usuarioRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao excluir usuário");
        }
    }
    
    //SALVAR NOVO USUÁRIO AJAX
    @PostMapping("/salvar-ajax")
    @ResponseBody
    public java.util.Map<String, Object> salvarAjax(

            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam String role,

            @RequestParam(required = false) 
            org.springframework.web.multipart.MultipartFile foto) {
        Usuario usuario = new Usuario();

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(senha);
        usuario.setRole(br.appLogin.appLogin.model.Role.valueOf(role));
        
        try {
        	System.out.println("Foto recebida: " + foto);
        	
        	if (foto != null) {
        	    System.out.println("Nome arquivo: " + foto.getOriginalFilename());
        	}

            // SE EXISTIR FOTO
            if (foto != null && !foto.isEmpty()) {

                java.util.Map<String, Object> uploadResult =
                        cloudinary.uploader().upload(
                                foto.getBytes(),
                                com.cloudinary.utils.ObjectUtils.emptyMap()
                        );

                // URL DA IMAGEM
                String imageUrl =
                        uploadResult.get("secure_url").toString();

                usuario.setFoto(imageUrl);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        usuarioRepository.save(usuario);

        // REGRA DE NEGÓCIO
        if (usuario.getRole() == br.appLogin.appLogin.model.Role.ALUNO) {

            // EVITAR DUPLICAÇÃO
            if (!alunoRepository.findByUsuarioEmail(email).isPresent()) {

                Aluno aluno = new Aluno();
                aluno.setUsuario(usuario);

                // GERAR RA AUTOMÁTICO NÃO NULL
                aluno.setRa("RA" + usuario.getId());

                alunoRepository.save(aluno);
            }
        }

        if (usuario.getRole() == br.appLogin.appLogin.model.Role.PROFESSOR) {

            if (!professorRepository.findByUsuarioId(usuario.getId()).isPresent()) {

                Professor professor = new Professor();
                professor.setUsuario(usuario);
                professor.setNome(usuario.getNome());

                professorRepository.save(professor);
            }
        }

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", usuario.getId());
        response.put("nome", usuario.getNome());
        response.put("email", usuario.getEmail());
        response.put("role", usuario.getRole());

        return response;
    }
    
    // LISTAR AJAX
    @GetMapping("/listar-ajax")
    @ResponseBody
    public java.util.List<Usuario> listarAjax() {
        return usuarioRepository.findAll();
    }
    
    // EDITAR AJAX
    @PutMapping("/editar-ajax/{id}")
    @ResponseBody
    public java.util.Map<String, Object> editarAjax(
            @PathVariable Long id,
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam String role,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(senha);
        usuario.setRole(br.appLogin.appLogin.model.Role.valueOf(role));

        // SE ENVIOU NOVA FOTO
        if (foto != null && !foto.isEmpty()) {

            System.out.println("Foto recebida: " + foto);
            System.out.println("Nome arquivo: " + foto.getOriginalFilename());

            try {

                Map uploadResult = cloudinary.uploader().upload(
                        foto.getBytes(),
                        ObjectUtils.emptyMap()
                );

                String fotoUrl = uploadResult.get("secure_url").toString();

                usuario.setFoto(fotoUrl);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        usuarioRepository.save(usuario);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", usuario.getId());
        response.put("nome", usuario.getNome());
        response.put("email", usuario.getEmail());
        response.put("role", usuario.getRole());
        response.put("foto", usuario.getFoto());

        return response;
    }
}