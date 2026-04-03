package com.ifsp.projeto.controller;

import com.ifsp.projeto.controller.dto.OrcamentoRequest;
import com.ifsp.projeto.service.OrcamentoService;
import com.ifsp.projeto.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrcamentoController {

    private final OrcamentoService orcamentoService;
    private final ProdutoService produtoService;

    public OrcamentoController(OrcamentoService orcamentoService, ProdutoService produtoService) {
        this.orcamentoService = orcamentoService;
        this.produtoService = produtoService;
    }

    @GetMapping("/orcamentos")
    public String listarOrcamentos(Model model) {
        model.addAttribute("orcamentos", orcamentoService.findAll());
        return "listaOrcamentos";
    }

    @GetMapping("/orcamento/novo")
    public String novoOrcamento(Model model) {
        model.addAttribute("orcamento", new OrcamentoRequest());
        model.addAttribute("produtos", produtoService.findAll());
        return "formularioOrcamento";
    }

    @PostMapping("/orcamento/salvar")
    @Transactional
    public String salvarOrcamento(@Valid OrcamentoRequest orcamentoRequest, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", result.getAllErrors());
            return "redirect:/orcamento/novo";
        }
        try {
            orcamentoService.salvarOrcamento(orcamentoRequest);
            redirectAttributes.addFlashAttribute("success", "Orçamento salvo com sucesso!");
            return "redirect:/orcamentos";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/orcamento/novo";
        }
    }

    @PostMapping("/orcamento/excluir/{id}")
    public String excluirOrcamento(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orcamentoService.excluirOrcamento(id);
            redirectAttributes.addFlashAttribute("success", "Orçamento excluído com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orcamentos";
    }

    @GetMapping("/orcamento/editar/{id}")
    public String editarOrcamento(@PathVariable Long id, Model model) {
        try {
            OrcamentoRequest orcamentoRequest = orcamentoService.findOrcamentoRequestById(id);
            model.addAttribute("orcamento", orcamentoRequest);
            model.addAttribute("produtos", produtoService.findAll());
            return "formularioOrcamento";
        } catch (IllegalArgumentException e) {
            return "redirect:/orcamentos";
        }
    }
}