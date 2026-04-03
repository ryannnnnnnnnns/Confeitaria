package com.ifsp.projeto.controller;

import com.ifsp.projeto.model.MateriaPrima;
import com.ifsp.projeto.repository.MateriaPrimaRepository;
import com.ifsp.projeto.service.MateriaPrimaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class MateriaPrimaController {

    private final MateriaPrimaService materiaPrimaService;
    private final MateriaPrimaRepository materiaPrimaRepository;

    public MateriaPrimaController(MateriaPrimaService materiaPrimaService, MateriaPrimaRepository materiaPrimaRepository) {
        this.materiaPrimaService = materiaPrimaService;
        this.materiaPrimaRepository = materiaPrimaRepository;
    }

    @GetMapping("/estoque")
    public String estoque(Model model,
                        @RequestParam(required = false) String nome,
                        @RequestParam(required = false) String unidade,
                        @RequestParam(required = false) String sort,
                        @RequestParam(required = false) String order) {
        model.addAttribute("materiasPrimas", materiaPrimaService.findAllWithAlert(nome, unidade, sort, order));
        return "tabelaEstoque";
    }

    @GetMapping("/entradaMP")
    public String entradaMP(Model model) {
        model.addAttribute("materiaPrima", new MateriaPrima());
        return "entradaMP";
    }

    @PostMapping("/adicionarMP")
    public String adicionarMP(MateriaPrima materiaPrima, Model model) {
        try {
            materiaPrimaService.adicionarMP(materiaPrima);
            return "redirect:/estoque";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("materiaPrima", new MateriaPrima());
            return "entradaMP";
        }
    }

    @GetMapping("/formularioMP")
    public String formularioMP(Model model) {
        model.addAttribute("materiaPrima", new MateriaPrima());
        model.addAttribute("materiasPrimas", materiaPrimaService.findAll());
        return "formularioMP";
    }

    @PostMapping("/salvarMP")
    public String salvarMP(MateriaPrima materiaPrima, RedirectAttributes redirectAttributes) {
        try {
            materiaPrimaService.salvarMP(materiaPrima);
            redirectAttributes.addFlashAttribute("success", "Matéria-prima salva com sucesso!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/estoque";
    }

    @GetMapping("/editarMP/{id}")
    public String editarMP(@PathVariable("id") Long id, Model model) {
        Optional<MateriaPrima> materiaPrimaOpt = materiaPrimaService.findById(id);
        if (materiaPrimaOpt.isPresent()) {
            model.addAttribute("materiaPrima", materiaPrimaOpt.get());
            return "formularioMP";
        }
        else {
            return "redirect:/estoque";
        }
    }

    @PostMapping("/excluirMP/{id}")
    public String excluirMP(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            materiaPrimaService.excluirMP(id);
            redirectAttributes.addFlashAttribute("success", "Matéria-prima excluída com sucesso.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/estoque";
    }

    @GetMapping("/api/materia-prima/sugestoes")
    @ResponseBody
    public List<MateriaPrima> sugerirMateriasPrimas(@RequestParam("termo") String termo) {
        return materiaPrimaRepository.findByNomeContainingIgnoreCase(termo);
    }
}