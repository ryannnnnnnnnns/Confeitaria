package com.ifsp.projeto.controller;

import com.ifsp.projeto.controller.dto.PedidoRequest;
import com.ifsp.projeto.model.Pedido;
import com.ifsp.projeto.service.PedidoService;
import com.ifsp.projeto.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class PedidoController {

    private final PedidoService pedidoService;
    private final ProdutoService produtoService;

    public PedidoController(PedidoService pedidoService, ProdutoService produtoService) {
        this.pedidoService = pedidoService;
        this.produtoService = produtoService;
    }

    @GetMapping("/pedidos")
    public String listarPedidos(@RequestParam(name = "cliente", required = false) String cliente,
                              @RequestParam(name = "status", required = false) String status,
                              @RequestParam(name = "data", required = false) String dataStr,
                              Model model) {
        List<Pedido> pedidos = pedidoService.findWithFilters(cliente, status, dataStr);
        model.addAttribute("pedidos", pedidos);
        return "listaPedidos";
    }

    @GetMapping("/pedido/novo")
    public String novoPedido(Model model) {
        model.addAttribute("pedido", new PedidoRequest());
        model.addAttribute("produtos", produtoService.findAll());
        return "formularioPedido";
    }

    @PostMapping("/pedido/salvar")
    public String salvarPedido(@Valid PedidoRequest pedidoRequest, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", result.getAllErrors());
            return "redirect:/pedido/novo";
        }
        try {
            pedidoService.salvarPedido(pedidoRequest);
            redirectAttributes.addFlashAttribute("success", "Pedido salvo com sucesso!");
            return "redirect:/pedidos";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedido/novo";
        }
    }

    @GetMapping("/pedido/{id}")
    public String detalhesPedido(@PathVariable Long id, Model model) {
        Pedido pedido = pedidoService.findByIdWithItens(id);
        model.addAttribute("pedido", pedido);
        return "detalhesPedido";
    }

    @PostMapping("/pedido/excluir/{id}")
    public String excluirPedido(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            pedidoService.excluirPedido(id);
            redirectAttributes.addFlashAttribute("success", "Pedido excluído com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos";
    }

    @PostMapping("/pedido/atualizar-status/{id}")
    public String atualizarStatusPedido(@PathVariable Long id, @RequestParam String status, RedirectAttributes redirectAttributes) {
        try {
            pedidoService.atualizarStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Status do pedido atualizado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos";
    }
}