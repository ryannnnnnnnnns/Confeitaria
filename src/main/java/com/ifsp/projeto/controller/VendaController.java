package com.ifsp.projeto.controller;

import com.ifsp.projeto.controller.dto.ItemVendaDTO;
import com.ifsp.projeto.controller.dto.ProducaoDisponivelDTO;
import com.ifsp.projeto.controller.dto.VendaDetalheDTO;
import com.ifsp.projeto.controller.dto.VendaEventoDTO;
import com.ifsp.projeto.controller.dto.VendaRequest;
import com.ifsp.projeto.model.ItemVenda;
import com.ifsp.projeto.model.Venda;
import com.ifsp.projeto.service.ProducaoService;
import com.ifsp.projeto.service.VendaService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class VendaController {

    private final VendaService vendaService;
    private final ProducaoService producaoService;

    public VendaController(VendaService vendaService, ProducaoService producaoService) {
        this.vendaService = vendaService;
        this.producaoService = producaoService;
    }

    @GetMapping("/vendas")
    public String vendas(Model model) {
        List<Venda> vendas = vendaService.findAllWithDetails();
        int totalQuantidade = vendas.stream().mapToInt(Venda::getQuantidade).sum();
        double totalValor = vendas.stream().mapToDouble(Venda::getValorVenda).sum();

        model.addAttribute("vendas", vendas);
        model.addAttribute("dataInicio", null);
        model.addAttribute("dataFim", null);
        model.addAttribute("totalQuantidade", totalQuantidade);
        model.addAttribute("totalValor", totalValor);
        return "relatorioVendas";
    }

    @GetMapping("/relatorio/vendas")
    public String relatorioVendas(@RequestParam(value = "dataInicio", required = false) String dataInicioStr,
                                  @RequestParam(value = "dataFim", required = false) String dataFimStr,
                                  Model model) {
        List<Venda> vendas = vendaService.findVendasByPeriod(dataInicioStr, dataFimStr);
        int totalQuantidade = vendas.stream().flatMap(venda -> venda.getItens().stream()).mapToInt(ItemVenda::getQuantidade).sum();
        double totalValor = vendas.stream().mapToDouble(Venda::getValorVenda).sum();

        model.addAttribute("vendas", vendas);
        model.addAttribute("dataInicio", dataInicioStr != null && !dataInicioStr.isEmpty() ? LocalDate.parse(dataInicioStr) : null);
        model.addAttribute("dataFim", dataFimStr != null && !dataFimStr.isEmpty() ? LocalDate.parse(dataFimStr) : null);
        model.addAttribute("totalQuantidade", totalQuantidade);
        model.addAttribute("totalValor", totalValor);

        return "relatorioVendas";
    }

    @GetMapping("/api/vendas/eventos")
    @ResponseBody
    public List<VendaEventoDTO> getVendaEventos() {
        return vendaService.getVendaEventos();
    }

    @GetMapping("/api/vendas/data")
    @ResponseBody
    public List<VendaDetalheDTO> getVendasPorData(@RequestParam("data") String data) {
        return vendaService.getVendasPorData(data);
    }

    @GetMapping("/vendas/dia")
    public String vendasDia(@RequestParam("data") String dataStr, Model model) {
        LocalDate data = LocalDate.parse(dataStr);
        List<Venda> vendas = vendaService.findByDataVendaWithProducaoAndProduto(data);
        model.addAttribute("vendas", vendas);
        model.addAttribute("data", data);
        return "vendasDia";
    }

    @GetMapping("/venda/nova")
    public String novaVenda(@RequestParam(value = "data", required = false) String dataStr, Model model) {
        LocalDate data;
        if (dataStr == null || dataStr.isEmpty()) {
            data = LocalDate.now();
        } else {
            data = LocalDate.parse(dataStr);
        }
        List<ProducaoDisponivelDTO> producoesDisponiveis = producaoService.findProducoesComEstoqueDisponivel();
        model.addAttribute("producoes", producoesDisponiveis);
        model.addAttribute("data", data);
        return "novaVenda";
    }

    @PostMapping("/venda/nova")
    @Transactional
    public String salvarVenda(@RequestParam Long producaoId,
                              @RequestParam int quantidade,
                              @RequestParam double valorVenda,
                              @RequestParam String formaPagamento,
                              @RequestParam(required = false) boolean doado,
                              @RequestParam String dataVenda,
                              RedirectAttributes redirectAttributes) {
        VendaRequest vendaRequest = new VendaRequest();
        vendaRequest.setDataVenda(dataVenda);
        vendaRequest.setFormaPagamento(formaPagamento);
        vendaRequest.setDoado(doado);

        ItemVendaDTO item = new ItemVendaDTO();
        item.setProducaoId(producaoId);
        item.setQuantidade(quantidade);
        item.setValorUnitario(valorVenda);

        vendaRequest.setItens(List.of(item));

        try {
            vendaService.salvarVenda(vendaRequest);
            redirectAttributes.addFlashAttribute("success", "Venda registrada com sucesso!");
            return "redirect:/vendas";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/venda/nova?data=" + dataVenda;
        }
    }

    @PostMapping("/venda/remover/{id}")
    public String removerVenda(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            vendaService.removerVenda(id);
            redirectAttributes.addFlashAttribute("success", "Venda removida com sucesso.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vendas";
    }

    @GetMapping("/venda/editar/{id}")
    public String editarVenda(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            VendaRequest vendaRequest = vendaService.findVendaRequestById(id);
            model.addAttribute("vendaRequest", vendaRequest);
            model.addAttribute("vendaId", id);
            List<ProducaoDisponivelDTO> producoesDisponiveis = producaoService.findProducoesComEstoqueDisponivel();
            model.addAttribute("producoes", producoesDisponiveis);
            return "formularioEditaVenda";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vendas";
        }
    }

    @PostMapping("/venda/editar/{id}")
    public String atualizarVenda(@PathVariable("id") Long id,
                                 @RequestParam Long producaoId,
                                 @RequestParam int quantidade,
                                 @RequestParam double valorVenda,
                                 @RequestParam String formaPagamento,
                                 @RequestParam(required = false) boolean doado,
                                 @RequestParam String dataVenda,
                                 RedirectAttributes redirectAttributes) {
        VendaRequest vendaRequest = new VendaRequest();
        vendaRequest.setDataVenda(dataVenda);
        vendaRequest.setFormaPagamento(formaPagamento);
        vendaRequest.setDoado(doado);

        ItemVendaDTO item = new ItemVendaDTO();
        item.setProducaoId(producaoId);
        item.setQuantidade(quantidade);
        item.setValorUnitario(valorVenda);

        vendaRequest.setItens(List.of(item));

        try {
            vendaService.atualizarVenda(id, vendaRequest);
            redirectAttributes.addFlashAttribute("success", "Venda atualizada com sucesso!");
            return "redirect:/vendas";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/venda/editar/" + id;
        }
    }
}