package com.ifsp.projeto.service;

import com.ifsp.projeto.model.*;
import com.ifsp.projeto.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para gerenciar a lógica de negócio relacionada a produtos.
 * Esta classe é responsável por criar, ler, atualizar e excluir produtos,
 * bem como calcular seus preços com base nos ingredientes.
 */
@Service
public class ProdutoService {

    @Value("${confeitaria.produto.markup}")
    private double markup;

    private final ProdutoRepository produtoRepository;
    private final IngredienteRepository ingredienteRepository;
    private final MateriaPrimaRepository materiaPrimaRepository;

    public ProdutoService(ProdutoRepository produtoRepository, IngredienteRepository ingredienteRepository, MateriaPrimaRepository materiaPrimaRepository) {
        this.produtoRepository = produtoRepository;
        this.ingredienteRepository = ingredienteRepository;
        this.materiaPrimaRepository = materiaPrimaRepository;
    }

    /**
     * Busca todos os produtos cadastrados no sistema.
     *
     * @return Uma lista de todos os produtos.
     */
    public List<Produto> findAll() {
        return produtoRepository.findAll();
    }

    /**
     * Busca um produto específico pelo seu ID, incluindo sua lista de ingredientes.
     *
     * @param id O ID do produto a ser buscado.
     * @return Um {@link Optional} contendo o produto com seus ingredientes, ou vazio se não for encontrado.
     */
    public Optional<Produto> findByIdWithIngredientes(Long id) {
        return produtoRepository.findByIdWithIngredientes(id);
    }

    /**
     * Salva um novo produto ou atualiza um existente.
     * Calcula o custo total com base nos ingredientes e define o preço de venda com uma margem de 30%.
     * Se o produto já existe, seus ingredientes antigos são removidos antes de adicionar os novos.
     *
     * @param produto O objeto {@link Produto} a ser salvo.
     * @param ingredientesIds Lista de IDs das matérias-primas que compõem o produto.
     * @param quantidades Lista de quantidades para cada matéria-prima, na mesma ordem de {@code ingredientesIds}.
     * @return O produto salvo com o preço calculado e a lista de ingredientes.
     */
    @Transactional
    public Produto salvarProduto(Produto produto, List<Long> ingredientesIds, List<Double> quantidades) {
        if (produto.getId() != null) {
            produtoRepository.findByIdWithIngredientes(produto.getId()).ifPresent(produtoExistente -> {
                ingredienteRepository.deleteAll(produtoExistente.getIngredientes());
            });
        }

        List<Ingrediente> receita = new ArrayList<>();
        if (ingredientesIds != null && !ingredientesIds.isEmpty() && quantidades != null && ingredientesIds.size() == quantidades.size()) {
            for (int i = 0; i < ingredientesIds.size(); i++) {
                Long ingredienteId = ingredientesIds.get(i);
                Double quantidade = quantidades.get(i);
                Optional<MateriaPrima> mpOpt = materiaPrimaRepository.findById(ingredienteId);
                if (mpOpt.isPresent()) {
                    Ingrediente ingrediente = new Ingrediente();
                    ingrediente.setProduto(produto);
                    ingrediente.setMateriaPrima(mpOpt.get());
                    ingrediente.setQuantidade(quantidade);
                    receita.add(ingrediente);
                }
            }
        }

        produto.setIngredientes(receita);

        double custoTotal = calcularCustoTotal(receita);
        double precoFinal = custoTotal * markup;
        produto.setPreco(precoFinal);

        return produtoRepository.save(produto);
    }

    /**
     * Recalcula o preço de venda de todos os produtos no sistema.
     * Itera sobre todos os produtos, recalcula o custo com base nos ingredientes e atualiza o preço se houver alteração.
     *
     * @return O número de produtos que tiveram seus preços atualizados.
     */
    @Transactional
    public int recalcularPrecos() {
        List<Produto> produtos = produtoRepository.findAll();
        int produtosAtualizados = 0;
        for (Produto produto : produtos) {
            Produto produtoComIngredientes = produtoRepository.findByIdWithIngredientes(produto.getId()).orElse(produto);

            double custoTotal = calcularCustoTotal(produtoComIngredientes.getIngredientes());
            double precoFinal = custoTotal * markup;

            if (produto.getPreco() == 0.0 || Math.abs(produto.getPreco() - precoFinal) > 0.01) {
                produto.setPreco(precoFinal);
                produtoRepository.save(produto);
                produtosAtualizados++;
            }
        }
        return produtosAtualizados;
    }

    /**
     * Atualiza o preço de um único produto com base no custo atual de seus ingredientes.
     *
     * @param id O ID do produto a ser atualizado.
     * @return O produto com o preço atualizado.
     * @throws IllegalArgumentException Se o produto com o ID fornecido não for encontrado.
     */
    @Transactional
    public Produto atualizarPrecoProduto(Long id) {
        Produto produto = produtoRepository.findByIdWithIngredientes(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));

        double custoTotal = calcularCustoTotal(produto.getIngredientes());
        double precoFinal = custoTotal * markup;
        produto.setPreco(precoFinal);

        return produtoRepository.save(produto);
    }

    /**
     * Exclui um produto do sistema com base no seu ID.
     *
     * @param id O ID do produto a ser excluído.
     */
    @Transactional
    public void excluirProduto(Long id) {
        produtoRepository.deleteById(id);
    }

    private double calcularCustoTotal(List<Ingrediente> ingredientes) {
        double custoTotal = 0.0;
        for (Ingrediente ingrediente : ingredientes) {
            if (ingrediente.getMateriaPrima() != null && ingrediente.getQuantidade() != null) {
                double valorUnitario = ingrediente.getMateriaPrima().getValor();
                custoTotal += ingrediente.getQuantidade() * valorUnitario;
            }
        }
        return custoTotal;
    }
}
