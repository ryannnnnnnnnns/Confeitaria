package com.ifsp.projeto.controller;

import com.ifsp.projeto.service.MateriaPrimaService;
import com.ifsp.projeto.service.PedidoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final MateriaPrimaService materiaPrimaService;
    private final PedidoService pedidoService;

    public HomeController(MateriaPrimaService materiaPrimaService, PedidoService pedidoService) {
        this.materiaPrimaService = materiaPrimaService;
        this.pedidoService = pedidoService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("materiasPrimasComEstoqueBaixo", materiaPrimaService.findLowStock());
        model.addAttribute("upcomingPedidos", pedidoService.findUpcomingPedidos());
        return "home";
    }
}
