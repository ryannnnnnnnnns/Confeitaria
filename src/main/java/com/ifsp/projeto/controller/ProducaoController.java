package com.ifsp.projeto.controller;

import com.ifsp.projeto.controller.dto.ProducaoDTO;
import com.ifsp.projeto.controller.dto.ProducaoEventoDTO;
import com.ifsp.projeto.controller.dto.ProducaoRequest;
import com.ifsp.projeto.model.Producao;
import com.ifsp.projeto.model.Produto;
import com.ifsp.projeto.service.ProducaoService;
import com.ifsp.projeto.service.ProdutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class ProducaoController {

    private static final Logger log = LoggerFactory.getLogger(ProducaoController.class);

    private final ProducaoService producaoService;
    private final ProdutoService produtoService;

    public ProducaoController(ProducaoService producaoService, ProdutoService produtoService) {
        this.producaoService = producaoService;
        this.produtoService = produtoService;
    }

    @GetMapping("/producao/registrar")
    public String registrarProducao(Model model, @RequestParam(value = "data", required = false) String data) {
        List<Produto> produtos = produtoService.findAll();
        model.addAttribute("produtos", produtos);
        if (data != null) {
            model.addAttribute("dataProducao", data);
        } else {
            model.addAttribute("dataProducao", LocalDate.now().toString());
        }
        return "producao";
    }

    @GetMapping("/producao")
    public String producaoIndex() {
        return "redirect:/producao/diaria";
    }

    @PostMapping("/producao/diaria")
    public String registrarProducao(@ModelAttribute("producaoRequest") ProducaoRequest producaoRequest, RedirectAttributes redirectAttributes) {
        LocalDate dataProducao = LocalDate.parse(producaoRequest.getDataProducao());

        if (producaoRequest.getProdutos() == null || producaoRequest.getProdutos().isEmpty()) {
            redirectAttributes.addFlashAttribute("errosDeEstoque", "Nenhum produto foi adicionado à produção.");
            return "redirect:/producao/registrar?data=" + producaoRequest.getDataProducao();
        }

        List<String> errosDeEstoque = producaoService.validarEstoque(producaoRequest.getProdutos());
        if (!errosDeEstoque.isEmpty()) {
            redirectAttributes.addFlashAttribute("errosDeEstoque", errosDeEstoque);
            return "redirect:/producao/registrar?data=" + producaoRequest.getDataProducao();
        }

        producaoService.registrarProducao(producaoRequest.getProdutos(), dataProducao);
        return "redirect:/producao/diaria?data=" + dataProducao.toString();
    }

    @GetMapping("/producao/diaria")
    public String producaoDiaria(@RequestParam(value = "data", required = false) String dataStr, Model model) {
        LocalDate data;
        if (dataStr != null && !dataStr.isEmpty()) {
            data = LocalDate.parse(dataStr);
        } else {
            data = LocalDate.now();
        }
        List<Producao> producoes = producaoService.findByDataProducao(data);
        model.addAttribute("producoes", producoes);
        model.addAttribute("data", data);
        return "producaoDiaria";
    }

    @PostMapping("/producao/diaria/aumentar/{id}")
    public String aumentarProducao(@PathVariable("id") Long id) {
        producaoService.aumentarProducao(id);
        return "redirect:/producao/diaria?data=" + producaoService.findById(id).map(p -> p.getDataProducao().toString()).orElse("");
    }

    @PostMapping("/producao/diaria/diminuir/{id}")
    public String diminuirProducao(@PathVariable("id") Long id) {
        producaoService.diminuirProducao(id);
        return "redirect:/producao/diaria?data=" + producaoService.findById(id).map(p -> p.getDataProducao().toString()).orElse("");
    }

    @PostMapping("/producao/diaria/remover/{id}")
    @Transactional
    public String removerProducao(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        producaoService.removerProducao(id);
        redirectAttributes.addFlashAttribute("success", "Produção e vendas associadas removidas com sucesso.");
        return "redirect:/producao/diaria?data=" + producaoService.findById(id).map(p -> p.getDataProducao().toString()).orElse("");
    }

    @PostMapping("/producao/diaria/remover-quantidade")
    public String removerQuantidadeProducao(@RequestParam Long producaoId, 
                                            @RequestParam int quantidadeARemover, 
                                            RedirectAttributes redirectAttributes) {
        try {
            producaoService.removerQuantidadeProducao(producaoId, quantidadeARemover);
            redirectAttributes.addFlashAttribute("modalSuccess", quantidadeARemover + " iten(s) removido(s) da produção com sucesso. Estoque atualizado.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("modalError", e.getMessage());
        }
        return "redirect:/calendario-producao";
    }

    @GetMapping("/calendario-producao")
    public String calendarioProducao() {
        return "calendarioProducao";
    }

    @GetMapping("/api/producao/eventos")
    @ResponseBody
    public List<ProducaoEventoDTO> getProducaoEventos() {
        return producaoService.getProducaoEventos();
    }

    @GetMapping("/api/producao/data")
    @ResponseBody
    public List<Producao> getProducaoPorData(@RequestParam("data") String data) {
        return producaoService.getProducaoPorData(data);
    }
}