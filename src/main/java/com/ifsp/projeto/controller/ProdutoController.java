package com.ifsp.projeto.controller;

import com.ifsp.projeto.controller.dto.IngredienteDetalheDTO;
import com.ifsp.projeto.model.Ingrediente;
import com.ifsp.projeto.model.Produto;
import com.ifsp.projeto.repository.ProdutoRepository;
import com.ifsp.projeto.service.ProdutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class ProdutoController {

    private static final Logger log = LoggerFactory.getLogger(ProdutoController.class);

    private final ProdutoService produtoService;
    private final ProdutoRepository produtoRepository;

    public ProdutoController(ProdutoService produtoService, ProdutoRepository produtoRepository) {
        this.produtoService = produtoService;
        this.produtoRepository = produtoRepository;
    }

    @GetMapping("/formulario-produto")
    public String formularioProduto(Model model) {
        model.addAttribute("produto", new Produto());
        return "formularioProduto";
    }

    @GetMapping("/produtos")
    public String listarProdutos(Model model) {
        model.addAttribute("produtos", produtoService.findAll());
        return "listaProdutos";
    }

    @GetMapping("/produto/{id}")
    public String detalharProduto(@PathVariable("id") Long id, Model model) {
        Optional<Produto> produtoOpt = produtoService.findByIdWithIngredientes(id);
        if (produtoOpt.isPresent()) {
            Produto produto = produtoOpt.get();

            List<IngredienteDetalheDTO> ingredientesDetalhe = new ArrayList<>();
            for (Ingrediente ingrediente : produto.getIngredientes()) {
                if (ingrediente.getMateriaPrima() != null && ingrediente.getQuantidade() != null) {
                    double valorUnitario = ingrediente.getMateriaPrima().getValor();
                    double custoIngrediente = ingrediente.getQuantidade() * valorUnitario;

                    ingredientesDetalhe.add(new IngredienteDetalheDTO(
                        ingrediente.getMateriaPrima().getNome(),
                        ingrediente.getQuantidade(),
                        ingrediente.getMateriaPrima().getUnidade(),
                        valorUnitario,
                        custoIngrediente
                    ));
                }
            }

            model.addAttribute("produto", produto);
            model.addAttribute("ingredientesDetalhe", ingredientesDetalhe);
            return "detalhesProduto";
        }
        return "redirect:/produtos?error=notfound";
    }

    @PostMapping("/produto/atualizar-preco/{id}")
    public String atualizarPrecoProduto(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            produtoService.atualizarPrecoProduto(id);
            redirectAttributes.addFlashAttribute("success", "Preço do produto atualizado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/produtos";
        }
        return "redirect:/produto/" + id;
    }

    @GetMapping("/editar-produto/{id}")
    public String editarProduto(@PathVariable("id") Long id, Model model) {
        Optional<Produto> produtoOpt = produtoService.findByIdWithIngredientes(id);
        if (produtoOpt.isPresent()) {
            model.addAttribute("produto", produtoOpt.get());
            return "formularioProduto";
        }
        else {
            return "redirect:/produtos";
        }
    }

    @PostMapping("/excluir-produto/{id}")
    public String excluirProduto(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            produtoService.excluirProduto(id);
            redirectAttributes.addFlashAttribute("success", "Produto e todos os dados associados (produções, vendas, pedidos e orçamentos) foram excluídos com sucesso.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/produtos";
    }

    @PostMapping("/salvar-produto")
    public String salvarProduto(Produto produto, @RequestParam(value = "ingredientesIds", required = false) List<Long> ingredientesIds, @RequestParam(value = "quantidades", required = false) List<Double> quantidades) {
        log.info("Recebendo requisição para salvar produto: {}", produto.getNome().replaceAll("[\n\r]", "_"));
        produtoService.salvarProduto(produto, ingredientesIds, quantidades);
        return "redirect:/produtos";
    }

    @GetMapping("/admin/recalcular-precos")
    public String recalcularPrecos(RedirectAttributes redirectAttributes) {
        int produtosAtualizados = produtoService.recalcularPrecos();
        redirectAttributes.addFlashAttribute("success", produtosAtualizados + " produto(s) tiveram seus preços recalculados e atualizados com sucesso!");
        return "redirect:/produtos";
    }

    @GetMapping("/api/produtos/sugestoes")
    @ResponseBody
    public List<Produto> sugerirProdutos(@RequestParam("termo") String termo) {
        return produtoRepository.findByNomeContainingIgnoreCase(termo);
    }
}