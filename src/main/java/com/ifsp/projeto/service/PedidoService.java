package com.ifsp.projeto.service;

import com.ifsp.projeto.controller.dto.ItemPedidoDTO;
import com.ifsp.projeto.controller.dto.PedidoRequest;
import com.ifsp.projeto.model.ItemPedido;
import com.ifsp.projeto.model.Pedido;
import com.ifsp.projeto.model.Produto;
import com.ifsp.projeto.repository.PedidoRepository;
import com.ifsp.projeto.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para gerenciar a lógica de negócio relacionada a pedidos de clientes.
 * Responsável por criar novos pedidos, buscar pedidos com filtros, atualizar status e excluir registros.
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
    }

    /**
     * Busca todos os pedidos com data de entrega a partir de hoje.
     *
     * @return Uma lista de pedidos futuros.
     */
    public List<Pedido> findUpcomingPedidos() {
        return pedidoRepository.findUpcomingPedidos(LocalDate.now());
    }

    /**
     * Busca pedidos com base em filtros de cliente, status e data de entrega.
     *
     * @param cliente Nome do cliente para busca (parcial).
     * @param status  Status do pedido (ex: 'Pendente', 'Concluído').
     * @param dataStr Data de entrega no formato de string (yyyy-MM-dd).
     * @return Lista de {@link Pedido} que correspondem aos filtros.
     */
    public List<Pedido> findWithFilters(String cliente, String status, String dataStr) {
        LocalDate data = null;
        if (dataStr != null && !dataStr.isEmpty()) {
            data = LocalDate.parse(dataStr);
        }
        return pedidoRepository.findWithFilters(cliente, status, data);
    }

    /**
     * Busca um pedido específico pelo seu ID, incluindo seus itens.
     *
     * @param id O ID do pedido a ser buscado.
     * @return O {@link Pedido} com seus itens, ou null se não for encontrado.
     */
    public Pedido findByIdWithItens(Long id) {
        return pedidoRepository.findByIdWithItens(id);
    }

    /**
     * Cria e salva um novo pedido com base nos dados da requisição.
     * Define o status inicial como 'Pendente' e associa os itens ao pedido.
     *
     * @param pedidoRequest O objeto de requisição ({@link PedidoRequest}) com os dados do pedido.
     * @return A entidade {@link Pedido} que foi salva.
     */
    @Transactional
    public Pedido salvarPedido(PedidoRequest pedidoRequest) {
        Pedido pedido = new Pedido();
        pedido.setCliente(pedidoRequest.getCliente());
        pedido.setDataEntrega(LocalDate.parse(pedidoRequest.getDataEntrega()));
        pedido.setStatus("Pendente");

        List<ItemPedido> itens = new ArrayList<>();
        for (ItemPedidoDTO itemDTO : pedidoRequest.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId()).orElse(null);
            if (produto != null) {
                ItemPedido item = new ItemPedido();
                item.setProduto(produto);
                item.setQuantidade(itemDTO.getQuantidade());
                item.setMassa(itemDTO.getMassa());
                item.setRecheio(itemDTO.getRecheio());
                item.setDetalhes(itemDTO.getDetalhes());
                item.setPedido(pedido);
                itens.add(item);
            }
        }
        pedido.setItens(itens);

        return pedidoRepository.save(pedido);
    }

    /**
     * Exclui um pedido do sistema com base no seu ID.
     *
     * @param id O ID do pedido a ser excluído.
     */
    @Transactional
    public void excluirPedido(Long id) {
        pedidoRepository.deleteById(id);
    }

    /**
     * Atualiza o status de um pedido existente.
     *
     * @param id     O ID do pedido a ser atualizado.
     * @param status O novo status do pedido.
     * @return O pedido com o status atualizado.
     * @throws IllegalArgumentException Se o pedido com o ID fornecido não for encontrado.
     */
    @Transactional
    public Pedido atualizarStatus(Long id, String status) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado!"));
        pedido.setStatus(status);
        return pedidoRepository.save(pedido);
    }
}