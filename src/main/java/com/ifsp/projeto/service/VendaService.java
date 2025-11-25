package com.ifsp.projeto.service;

import com.ifsp.projeto.controller.dto.ItemVendaDTO;
import com.ifsp.projeto.controller.dto.VendaDetalheDTO;
import com.ifsp.projeto.controller.dto.VendaEventoDTO;
import com.ifsp.projeto.controller.dto.VendaRequest;
import com.ifsp.projeto.model.ItemVenda;
import com.ifsp.projeto.model.Producao;
import com.ifsp.projeto.model.Venda;
import com.ifsp.projeto.repository.ItemVendaRepository;
import com.ifsp.projeto.repository.ProducaoRepository;
import com.ifsp.projeto.repository.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciar a lógica de negócio relacionada a vendas.
 * Responsável por criar novas vendas, validar estoque, buscar vendas para relatórios e excluir registros.
 */
@Service
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ProducaoRepository producaoRepository;
    private final ItemVendaRepository itemVendaRepository;

    public VendaService(VendaRepository vendaRepository, ProducaoRepository producaoRepository, ItemVendaRepository itemVendaRepository) {
        this.vendaRepository = vendaRepository;
        this.producaoRepository = producaoRepository;
        this.itemVendaRepository = itemVendaRepository;
    }

    /**
     * Agrupa as vendas por data para exibição em um calendário de eventos.
     *
     * @return Lista de {@link VendaEventoDTO} contendo o título do evento e a data.
     */
    public List<VendaEventoDTO> getVendaEventos() {
        return vendaRepository.findAll().stream()
                .collect(Collectors.groupingBy(Venda::getDataVenda))
                .entrySet().stream()
                .map(entry -> {
                    long count = entry.getValue().size();
                    String title = count + (count == 1 ? " venda" : " vendas");
                    return new VendaEventoDTO(title, entry.getKey().toString());
                })
                .collect(Collectors.toList());
    }

    /**
     * Busca os detalhes de todas as vendas realizadas em uma data específica.
     *
     * @param data A data no formato de string (yyyy-MM-dd).
     * @return Lista de {@link VendaDetalheDTO} com os detalhes de cada item de venda.
     */
    public List<VendaDetalheDTO> getVendasPorData(String data) {
        LocalDate localDate = LocalDate.parse(data);
        List<Venda> vendas = vendaRepository.findByDataVendaWithProducaoAndProduto(localDate);
        return vendas.stream()
                .flatMap(v -> v.getItens().stream())
                .map(item -> new VendaDetalheDTO(
                        item.getVenda().getId(),
                        item.getProducao().getProduto().getNome(),
                        item.getValorTotal(),
                        item.getVenda().getFormaPagamento(),
                        item.getVenda().isDoado()))
                .collect(Collectors.toList());
    }

    /**
     * Busca vendas por data, incluindo detalhes da produção e do produto.
     *
     * @param data A data da busca.
     * @return Lista de vendas ({@link Venda}) com seus detalhes.
     */
    public List<Venda> findByDataVendaWithProducaoAndProduto(LocalDate data) {
        return vendaRepository.findByDataVendaWithProducaoAndProduto(data);
    }

    /**
     * Filtra as vendas por um período de datas. Se as datas de início e fim não forem fornecidas, retorna todas as vendas.
     *
     * @param dataInicioStr Data de início do período (formato yyyy-MM-dd).
     * @param dataFimStr    Data de fim do período (formato yyyy-MM-dd).
     * @return Lista de vendas ({@link Venda}) encontradas no período, ordenadas da mais recente para a mais antiga.
     */
    public List<Venda> findVendasByPeriod(String dataInicioStr, String dataFimStr) {
        LocalDate dataInicio = null;
        LocalDate dataFim = null;

        if (dataInicioStr != null && !dataInicioStr.isEmpty() && dataFimStr != null && !dataFimStr.isEmpty()) {
            dataInicio = LocalDate.parse(dataInicioStr);
            dataFim = LocalDate.parse(dataFimStr);
            return vendaRepository.findByDataVendaBetweenWithDetailsOrderByDataVendaDesc(dataInicio, dataFim);
        } else {
            return vendaRepository.findAllWithDetailsOrderByDataVendaDesc();
        }
    }

    /**
     * Cria e salva uma nova venda com base nos dados recebidos.
     * Antes de salvar, valida se a quantidade de cada item solicitado está disponível no estoque de produção.
     * Calcula os totais e associa os itens à venda.
     *
     * @param vendaRequest O objeto de requisição ({@link VendaRequest}) contendo os dados da venda e dos itens.
     * @return A entidade {@link Venda} que foi salva no banco de dados.
     * @throws IllegalArgumentException Se a quantidade de um item exceder o estoque ou se nenhum item válido for fornecido.
     */
    @Transactional
    public Venda salvarVenda(VendaRequest vendaRequest) {
        validateStock(null, vendaRequest.getItens());

        Venda venda = new Venda();
        venda.setFormaPagamento(vendaRequest.getFormaPagamento());
        venda.setDoado(vendaRequest.isDoado());
        venda.setDataVenda(LocalDate.parse(vendaRequest.getDataVenda()));

        List<ItemVenda> itensVenda = vendaRequest.getItens().stream()
                .filter(itemDTO -> itemDTO.getProducaoId() != null && itemDTO.getQuantidade() > 0)
                .map(itemDTO -> createItemVenda(itemDTO, vendaRequest, venda))
                .collect(Collectors.toList());


        if (itensVenda.isEmpty()) {
            throw new IllegalArgumentException("Nenhum item foi adicionado à venda.");
        }

        double valorTotalVenda = itensVenda.stream().mapToDouble(ItemVenda::getValorTotal).sum();
        int quantidadeTotal = itensVenda.stream().mapToInt(ItemVenda::getQuantidade).sum();

        venda.setItens(itensVenda);
        venda.setValorVenda(valorTotalVenda);
        venda.setQuantidade(quantidadeTotal);

        return vendaRepository.save(venda);
    }

    private void validateStock(Long vendaId, List<ItemVendaDTO> itens) {
        for (ItemVendaDTO itemDTO : itens) {
            if (itemDTO.getProducaoId() == null || itemDTO.getQuantidade() <= 0) {
                continue;
            }
            Producao producao = producaoRepository.findById(itemDTO.getProducaoId())
                    .orElseThrow(() -> new IllegalArgumentException("Produção inválida Id:" + itemDTO.getProducaoId()));

            long quantidadeJaVendida;
            if (vendaId != null) {
                quantidadeJaVendida = itemVendaRepository.sumQuantidadeByProducaoIdAndVendaIdNot(producao.getId(), vendaId);
            } else {
                quantidadeJaVendida = itemVendaRepository.sumQuantidadeByProducaoId(producao.getId());
            }

            long quantidadeDisponivel = producao.getQuantidade() - quantidadeJaVendida;

            if (itemDTO.getQuantidade() > quantidadeDisponivel) {
                throw new IllegalArgumentException("A quantidade solicitada de '" + producao.getProduto().getNome() + "' (" + itemDTO.getQuantidade() + ") excede o estoque disponível (" + quantidadeDisponivel + ").");
            }
        }
    }

    private ItemVenda createItemVenda(ItemVendaDTO itemDTO, VendaRequest request, Venda venda) {
        Producao producao = producaoRepository.findById(itemDTO.getProducaoId())
                .orElseThrow(() -> new IllegalArgumentException("Produção inválida Id:" + itemDTO.getProducaoId()));

        ItemVenda itemVenda = new ItemVenda();
        itemVenda.setProducao(producao);
        itemVenda.setQuantidade(itemDTO.getQuantidade());
        itemVenda.setVenda(venda);

        double valorUnitario = request.isDoado() ? 0 : itemDTO.getValorUnitario();
        itemVenda.setValorUnitario(valorUnitario);
        itemVenda.setValorTotal(valorUnitario * itemDTO.getQuantidade());

        return itemVenda;
    }


    /**
     * Atualiza uma venda existente.
     *
     * @param id O ID da venda a ser atualizada.
     * @param vendaRequest O objeto de requisição com os novos dados da venda.
     * @return A entidade {@link Venda} que foi atualizada.
     * @throws IllegalArgumentException Se a venda não for encontrada ou se o estoque for insuficiente.
     */
    /**
     * Remove uma venda do sistema pelo seu ID.
     *
     * @param id O ID da venda a ser removida.
     * @throws IllegalArgumentException Se a venda com o ID fornecido não existir.
     */
    @Transactional
    public void removerVenda(Long id) {
        if (!vendaRepository.existsById(id)) {
            throw new IllegalArgumentException("Venda não encontrada.");
        }
        vendaRepository.deleteById(id);
    }

    /**
     * Busca todas as vendas com seus itens detalhados.
     *
     * @return Uma lista de todas as vendas, ordenadas da mais recente para a mais antiga.
     */
    public List<Venda> findAllWithDetails() {
        return vendaRepository.findAllWithDetailsOrderByDataVendaDesc();
    }

    @Transactional(readOnly = true)
    public VendaRequest findVendaRequestById(Long id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada com o id: " + id));
        return new VendaRequest(venda);
    }

    /**
     * Atualiza uma venda existente.
     *
     * @param id O ID da venda a ser atualizada.
     * @param vendaRequest O objeto de requisição com os novos dados da venda.
     * @return A entidade {@link Venda} que foi atualizada.
     * @throws IllegalArgumentException Se a venda não for encontrada ou se o estoque for insuficiente.
     */
    @Transactional
    public Venda atualizarVenda(Long id, VendaRequest vendaRequest) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada com o id: " + id));

        validateStock(id, vendaRequest.getItens());

        // Remove old items
        itemVendaRepository.deleteAll(venda.getItens());
        venda.getItens().clear();

        List<ItemVenda> novosItens = vendaRequest.getItens().stream()
                .filter(itemDTO -> itemDTO.getProducaoId() != null && itemDTO.getQuantidade() > 0)
                .map(itemDTO -> createItemVenda(itemDTO, vendaRequest, venda))
                .collect(Collectors.toList());

        if (novosItens.isEmpty()) {
            throw new IllegalArgumentException("Nenhum item foi adicionado à venda.");
        }

        double valorTotalVenda = novosItens.stream().mapToDouble(ItemVenda::getValorTotal).sum();
        int quantidadeTotal = novosItens.stream().mapToInt(ItemVenda::getQuantidade).sum();

        venda.setItens(novosItens);
        venda.setValorVenda(valorTotalVenda);
        venda.setQuantidade(quantidadeTotal);
        venda.setDataVenda(LocalDate.parse(vendaRequest.getDataVenda()));
        venda.setFormaPagamento(vendaRequest.getFormaPagamento());
        venda.setDoado(vendaRequest.isDoado());

        return vendaRepository.save(venda);
    }
}