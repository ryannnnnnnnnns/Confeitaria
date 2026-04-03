package com.ifsp.projeto.controller;

import com.ifsp.projeto.model.Usuario;
import com.ifsp.projeto.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login() {
        return "telaLogin";
    }

    @GetMapping("/configuracoes")
    public String configuracoes(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        return "configuracoes";
    }

    @PostMapping("/redefinir-senha")
    public String redefinirSenha(@RequestParam String senhaAntiga,
                                 @RequestParam String novaSenha,
                                 @RequestParam String confirmarNovaSenha,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            usuarioService.redefinirSenha(principal.getName(), senhaAntiga, novaSenha, confirmarNovaSenha);
            redirectAttributes.addFlashAttribute("success", "Senha redefinida com sucesso.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/configuracoes";
    }

    @PostMapping("/redefinir-login")
    public String redefinirLogin(@RequestParam String loginAtual,
                                 @RequestParam String novoLogin,
                                 @RequestParam String senha,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            usuarioService.redefinirLogin(principal.getName(), loginAtual, novoLogin, senha);
            redirectAttributes.addFlashAttribute("successLogin", "Nome de usuário redefinido com sucesso. Por favor, faça login novamente.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorLogin", e.getMessage());
            return "redirect:/configuracoes";
        }
    }

    @GetMapping("/esqueci-senha")
    public String esqueciSenha() {
        return "esqueciSenha";
    }

    @GetMapping("/cadastroUsuario")
    public String cadastroUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "cadastroUsuario";
    }

    @PostMapping("/salvarUsuario")
    public String salvarUsuario(@ModelAttribute("usuario") Usuario usuario, @RequestParam("confirmarSenha") String confirmarSenha, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.salvarUsuario(usuario, confirmarSenha);
            redirectAttributes.addFlashAttribute("success", "Usuário cadastrado com sucesso! Aguarde a aprovação do administrador.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cadastroUsuario";
        }
    }

    @PostMapping("/esqueci-senha")
    public String processEsqueciSenha(@RequestParam String login, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.processEsqueciSenha(login);
            redirectAttributes.addAttribute("login", login);
            return "redirect:/redefinir-senha-pergunta";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/esqueci-senha";
        }
    }

    @GetMapping("/redefinir-senha-pergunta")
    public String redefinirSenhaPergunta(@RequestParam String login, Model model) {
        usuarioService.findByLogin(login).ifPresent(usuario -> model.addAttribute("usuario", usuario));
        return "redefinirSenhaPergunta";
    }

    @PostMapping("/verificar-resposta")
    public String verificarResposta(@RequestParam String login, @RequestParam String respostaSecreta, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.verificarResposta(login, respostaSecreta);
            redirectAttributes.addAttribute("login", login);
            return "redirect:/nova-senha";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addAttribute("login", login);
            return "redirect:/redefinir-senha-pergunta";
        }
    }

    @GetMapping("/nova-senha")
    public String novaSenha(@RequestParam String login, Model model) {
        model.addAttribute("login", login);
        return "novaSenha";
    }

    @PostMapping("/salvar-nova-senha")
    public String salvarNovaSenha(@RequestParam String login, @RequestParam String novaSenha, @RequestParam String confirmarNovaSenha, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.salvarNovaSenha(login, novaSenha, confirmarNovaSenha);
            redirectAttributes.addFlashAttribute("success", "Senha redefinida com sucesso.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addAttribute("login", login);
            return "redirect:/nova-senha";
        }
    }

    @PostMapping("/redefinir-pergunta")
    public String redefinirPergunta(@RequestParam String senha, @RequestParam String novaPerguntaSecreta, @RequestParam String novaRespostaSecreta, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.redefinirPergunta(principal.getName(), senha, novaPerguntaSecreta, novaRespostaSecreta);
            redirectAttributes.addFlashAttribute("successPergunta", "Pergunta secreta redefinida com sucesso.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorPergunta", e.getMessage());
        }
        return "redirect:/configuracoes";
    }

    @GetMapping("/admin/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.findAll());
        return "listaUsuarios";
    }

    @PostMapping("/admin/usuarios/aprovar/{id}")
    public String aprovarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.aprovarUsuario(id);
            redirectAttributes.addFlashAttribute("success", "Usuário aprovado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/admin/usuarios/reprovar/{id}")
    public String reprovarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.reprovarUsuario(id);
            redirectAttributes.addFlashAttribute("success", "Usuário reprovado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/admin/usuarios/excluir/{id}")
    public String excluirUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.excluirUsuario(id);
        redirectAttributes.addFlashAttribute("success", "Usuário excluído com sucesso!");
        return "redirect:/admin/usuarios";
    }
}