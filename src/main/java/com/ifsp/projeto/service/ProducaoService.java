package com.ifsp.projeto.service;

import com.ifsp.projeto.controller.dto.ProducaoDTO;
import com.ifsp.projeto.controller.dto.ProducaoDisponivelDTO;
import com.ifsp.projeto.controller.dto.ProducaoEventoDTO;
import com.ifsp.projeto.model.Ingrediente;
import com.ifsp.projeto.model.MateriaPrima;
import com.ifsp.projeto.model.Producao;
import com.ifsp.projeto.model.Produto;
import com.ifsp.projeto.repository.ItemVendaRepository;
import com.ifsp.projeto.repository.MateriaPrimaRepository;
import com.ifsp.projeto.repository.ProducaoRepository;
import com.ifsp.projeto.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciar a lógica de negócio da produção de itens.
 * Responsável por registrar novas produções, o que implica em consumir matéria-prima do estoque.
 * Também gerencia o ajuste e a remoção de produções, devolvendo a matéria-prima ao estoque.
 */
@Service
public class ProducaoService {

    private final ProducaoRepository producaoRepository;
    private final ProdutoRepository produtoRepository;
    private final MateriaPrimaRepository materiaPrimaRepository;
    private final ItemVendaRepository itemVendaRepository;

    public ProducaoService(ProducaoRepository producaoRepository, ProdutoRepository produtoRepository, MateriaPrimaRepository materiaPrimaRepository, ItemVendaRepository itemVendaRepository) {
        this.producaoRepository = producaoRepository;
        this.produtoRepository = produtoRepository;
        this.materiaPrimaRepository = materiaPrimaRepository;
        this.itemVendaRepository = itemVendaRepository;
    }

    /**
     * Busca todos os registros de produção de uma data específica.
     * @param data A data da produção.
     * @return Lista de {@link Producao} da data especificada.
     */
    public List<Producao> findByDataProducao(LocalDate data) {
        return producaoRepository.findByDataProducao(data);
    }

    /**
     * Busca um registro de produção pelo seu ID.
     * @param id O ID da produção.
     * @return Um {@link Optional} contendo a produção, ou vazio se não encontrada.
     */
    public Optional<Producao> findById(Long id) {
        return producaoRepository.findById(id);
    }

    /**
     * Agrupa os registros de produção por data para exibição em calendário.
     * @return Lista de {@link ProducaoEventoDTO} com o resumo da produção de cada dia.
     */
    public List<ProducaoEventoDTO> getProducaoEventos() {
        return producaoRepository.findAllWithProduto().stream()
                .collect(Collectors.groupingBy(Producao::getDataProducao))
                .entrySet().stream()
                .map(entry -> {
                    long totalItens = entry.getValue().stream()
                                           .mapToLong(Producao::getQuantidade)
                                           .sum();
                    String title = totalItens + (totalItens == 1 ? " item produzido" : " itens produzidos");
                    return new ProducaoEventoDTO(title, entry.getKey().toString());
                })
                .collect(Collectors.toList());
    }

    /**
     * Busca os registros de produção de uma data específica.
     * @param data A data no formato de string (yyyy-MM-dd).
     * @return Lista de {@link Producao} da data especificada.
     */
    public List<Producao> getProducaoPorData(String data) {
        LocalDate localDate = LocalDate.parse(data);
        return producaoRepository.findByDataProducao(localDate);
    }

    /**
     * Busca todas as produções que ainda possuem estoque disponível para venda.
     * @return Lista de {@link ProducaoDisponivelDTO} representando as produções com estoque.
     */
    public List<ProducaoDisponivelDTO> findProducoesComEstoqueDisponivel() {
        return producaoRepository.findProducoesComEstoqueDisponivel();
    }

    public List<ProducaoDisponivelDTO> findProducoesComEstoqueDisponivelParaVenda(Long vendaId) {
        return producaoRepository.findProducoesComEstoqueDisponivelParaVenda(vendaId);
    }

    /**
     * Remove uma quantidade específica de um lote de produção e devolve os ingredientes ao estoque.
     * @param producaoId O ID do lote de produção.
     * @param quantidadeARemover A quantidade de itens a ser removida.
     * @throws IllegalArgumentException Se a produção não for encontrada ou a quantidade a remover for inválida.
     */
    @Transactional
    public void removerQuantidadeProducao(Long producaoId, int quantidadeARemover) {
        if (quantidadeARemover <= 0) {
            throw new IllegalArgumentException("A quantidade a ser removida deve ser maior que zero.");
        }

        Producao producao = producaoRepository.findById(producaoId)
                .orElseThrow(() -> new IllegalArgumentException("Produção não encontrada."));

        int quantidadeAtual = producao.getQuantidade();

        if (quantidadeARemover > quantidadeAtual) {
            throw new IllegalArgumentException("Não é possível remover mais itens do que foram produzidos.");
        }

        if (quantidadeARemover == quantidadeAtual) {
            producaoRepository.delete(producao);
        } else {
            producao.setQuantidade(quantidadeAtual - quantidadeARemover);
            producaoRepository.save(producao);
        }

        Produto produto = produtoRepository.findByIdWithIngredientes(producao.getProduto().getId())
                                           .orElse(producao.getProduto());

        devolverEstoque(produto, quantidadeARemover);
    }

    /**
     * Registra uma nova produção de um ou mais produtos, consumindo a matéria-prima necessária do estoque.
     * @param producoes Lista de {@link ProducaoDTO} com os dados dos produtos a serem produzidos.
     * @param dataProducao A data em que a produção foi realizada.
     */
    @Transactional
    public void registrarProducao(List<ProducaoDTO> producoes, LocalDate dataProducao) {
        for (ProducaoDTO producaoDTO : producoes) {
            if (producaoDTO.getQuantidade() > 0) {
                produtoRepository.findByIdWithIngredientes(producaoDTO.getProdutoId()).ifPresent(produto -> {
                    atualizarEstoque(produto, producaoDTO.getQuantidade());
                    Producao producao = new Producao();
                    producao.setProduto(produto);
                    producao.setQuantidade(producaoDTO.getQuantidade());
                    producao.setDataProducao(dataProducao);
                    producao.setMassa(producaoDTO.getMassa());
                    producao.setRecheio(producaoDTO.getRecheio());
                    producaoRepository.save(producao);
                });
            }
        }
    }

    /**
     * Aumenta em uma unidade a quantidade de um lote de produção e consome os ingredientes do estoque.
     * @param id O ID do lote de produção a ser incrementado.
     */
    @Transactional
    public void aumentarProducao(Long id) {
        Optional<Producao> producaoOpt = producaoRepository.findById(id);
        if (producaoOpt.isPresent()) {
            Producao producao = producaoOpt.get();
            Produto produto = produtoRepository.findByIdWithIngredientes(producao.getProduto().getId()).orElse(producao.getProduto());

            producao.setQuantidade(producao.getQuantidade() + 1);
            producaoRepository.save(producao);

            atualizarEstoque(produto, 1);
        }
    }

    /**
     * Diminui em uma unidade a quantidade de um lote de produção e devolve os ingredientes ao estoque.
     * Se a quantidade chegar a zero, o lote de produção é removido.
     * @param id O ID do lote de produção a ser decrementado.
     */
    @Transactional
    public void diminuirProducao(Long id) {
        Optional<Producao> producaoOpt = producaoRepository.findById(id);
        if (producaoOpt.isPresent()) {
            Producao producao = producaoOpt.get();
            Produto produto = produtoRepository.findByIdWithIngredientes(producao.getProduto().getId()).orElse(producao.getProduto());
            if (producao.getQuantidade() > 1) {
                producao.setQuantidade(producao.getQuantidade() - 1);
                producaoRepository.save(producao);
                devolverEstoque(produto, 1);
            } else {
                producaoRepository.delete(producao);
                devolverEstoque(produto, 1);
            }
        }
    }

    /**
     * Remove completamente um lote de produção, incluindo itens de venda associados, e devolve todo o estoque de ingredientes.
     * @param id O ID do lote de produção a ser removido.
     */
    @Transactional
    public void removerProducao(Long id) {
        Optional<Producao> producaoOpt = producaoRepository.findById(id);
        if (producaoOpt.isPresent()) {
            Producao producao = producaoOpt.get();
            int quantidadeProduzida = producao.getQuantidade();
            Produto produto = producao.getProduto();

            itemVendaRepository.deleteByProducaoId(id);

            Produto produtoComIngredientes = produtoRepository.findByIdWithIngredientes(produto.getId()).orElse(produto);
            devolverEstoque(produtoComIngredientes, quantidadeProduzida);

            producaoRepository.delete(producao);
        }
    }

    /**
     * Valida se há estoque de matéria-prima suficiente para realizar uma lista de produções.
     * @param producoes A lista de produções planejadas.
     * @return Uma lista de strings com as mensagens de erro, ou uma lista vazia se o estoque for suficiente.
     */
    public List<String> validarEstoque(List<ProducaoDTO> producoes) {
        List<String> erros = new ArrayList<>();
        for (ProducaoDTO producaoDTO : producoes) {
            if (producaoDTO.getQuantidade() > 0) {
                produtoRepository.findByIdWithIngredientes(producaoDTO.getProdutoId()).ifPresent(produto -> {
                    for (Ingrediente ingrediente : produto.getIngredientes()) {
                        MateriaPrima mp = ingrediente.getMateriaPrima();
                        double quantidadeNecessaria = ingrediente.getQuantidade() * producaoDTO.getQuantidade();
                        if (mp.getQuantidade() < quantidadeNecessaria) {
                            erros.add(String.format("Estoque de '%s' insuficiente para '%s'. Necessário: %.2f, Disponível: %.2f",
                                    mp.getNome(), produto.getNome(), quantidadeNecessaria, mp.getQuantidade()));
                        }
                    }
                });
            }
        }
        return erros;
    }

    private void atualizarEstoque(Produto produto, int quantidade) {
        for (Ingrediente ingrediente : produto.getIngredientes()) {
            MateriaPrima mp = ingrediente.getMateriaPrima();
            double quantidadeNecessaria = ingrediente.getQuantidade() * quantidade;
            mp.setQuantidade(mp.getQuantidade() - quantidadeNecessaria);
            materiaPrimaRepository.save(mp);
        }
    }

    private void devolverEstoque(Produto produto, int quantidade) {
        for (Ingrediente ingrediente : produto.getIngredientes()) {
            MateriaPrima mp = ingrediente.getMateriaPrima();
            mp.setQuantidade(mp.getQuantidade() + (ingrediente.getQuantidade() * quantidade));
            materiaPrimaRepository.save(mp);
        }
    }
}