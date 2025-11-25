package com.ifsp.projeto.service;

import com.ifsp.projeto.controller.dto.ItemOrcamentoDTO;
import com.ifsp.projeto.controller.dto.OrcamentoRequest;
import com.ifsp.projeto.model.ItemOrcamento;
import com.ifsp.projeto.model.Orcamento;
import com.ifsp.projeto.model.Produto;
import com.ifsp.projeto.repository.ItemOrcamentoRepository;
import com.ifsp.projeto.repository.OrcamentoRepository;
import com.ifsp.projeto.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para gerenciar a lógica de negócio relacionada a orçamentos.
 * Responsável por criar, ler, atualizar e excluir orçamentos, incluindo o cálculo de valores com base nos itens e descontos.
 */
@Service
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final ProdutoRepository produtoRepository;
    private final ItemOrcamentoRepository itemOrcamentoRepository;

    public OrcamentoService(OrcamentoRepository orcamentoRepository, ProdutoRepository produtoRepository, ItemOrcamentoRepository itemOrcamentoRepository) {
        this.orcamentoRepository = orcamentoRepository;
        this.produtoRepository = produtoRepository;
        this.itemOrcamentoRepository = itemOrcamentoRepository;
    }

    /**
     * Busca todos os orçamentos cadastrados.
     *
     * @return Uma lista de todos os orçamentos.
     */
    public List<Orcamento> findAll() {
        return orcamentoRepository.findAll();
    }

    /**
     * Busca um orçamento pelo ID e o converte para um DTO de requisição, útil para popular formulários de edição.
     *
     * @param id O ID do orçamento a ser buscado.
     * @return Um {@link OrcamentoRequest} preenchido com os dados do orçamento.
     * @throws IllegalArgumentException Se o orçamento com o ID fornecido não for encontrado.
     */
    public OrcamentoRequest findOrcamentoRequestById(Long id) {
        Orcamento orcamento = orcamentoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Orçamento não encontrado"));
        OrcamentoRequest orcamentoRequest = new OrcamentoRequest();
        orcamentoRequest.setId(orcamento.getId());
        orcamentoRequest.setCliente(orcamento.getCliente());
        orcamentoRequest.setDesconto(orcamento.getDesconto());
        
        List<ItemOrcamentoDTO> itensDTO = new ArrayList<>();
        for (ItemOrcamento item : orcamento.getItens()) {
            ItemOrcamentoDTO itemDTO = new ItemOrcamentoDTO();
            itemDTO.setProdutoId(item.getProduto().getId());
            itemDTO.setQuantidade((int) item.getQuantidade());
            itemDTO.setValorUnitario(item.getValorUnitario());
            itensDTO.add(itemDTO);
        }
        orcamentoRequest.setItens(itensDTO);
        return orcamentoRequest;
    }

    /**
     * Salva um novo orçamento ou atualiza um existente com base nos dados da requisição.
     * Se um ID for fornecido, o orçamento existente é atualizado, e seus itens antigos são removidos.
     * Caso contrário, um novo orçamento é criado com status 'Pendente'.
     * O valor total e final (com desconto) são calculados.
     *
     * @param orcamentoRequest O objeto de requisição ({@link OrcamentoRequest}) com os dados do orçamento.
     * @return A entidade {@link Orcamento} que foi salva.
     */
    @Transactional
    public Orcamento salvarOrcamento(OrcamentoRequest orcamentoRequest) {
        if (orcamentoRequest.getDesconto() == null) {
            orcamentoRequest.setDesconto(0.0);
        }
        Orcamento orcamento;
        if (orcamentoRequest.getId() != null) {
            orcamento = orcamentoRepository.findById(orcamentoRequest.getId()).orElseThrow(() -> new IllegalArgumentException("Invalid orcamento Id:" + orcamentoRequest.getId()));
        } else {
            orcamento = new Orcamento();
            orcamento.setDataOrcamento(LocalDate.now());
            orcamento.setStatus("Pendente");
        }

        orcamento.setCliente(orcamentoRequest.getCliente());

        if (orcamento.getItens() != null) {
            itemOrcamentoRepository.deleteAll(orcamento.getItens());
            orcamento.getItens().clear();
        } else {
            orcamento.setItens(new ArrayList<>());
        }

        double valorTotal = 0;
        if (orcamentoRequest.getItens() != null) {
            for (ItemOrcamentoDTO itemDTO : orcamentoRequest.getItens()) {
                Produto produto = produtoRepository.findById(itemDTO.getProdutoId()).orElse(null);
                if (produto != null) {
                    ItemOrcamento item = new ItemOrcamento();
                    item.setProduto(produto);
                    item.setQuantidade(itemDTO.getQuantidade());
                    item.setValorUnitario(itemDTO.getValorUnitario());
                    double valorTotalItem = itemDTO.getQuantidade() * itemDTO.getValorUnitario();
                    item.setValorTotal(valorTotalItem);
                    item.setOrcamento(orcamento);
                    orcamento.getItens().add(item);
                    valorTotal += valorTotalItem;
                }
            }
        }

        orcamento.setValorTotal(valorTotal);
        double descontoPercentual = orcamentoRequest.getDesconto() != null ? orcamentoRequest.getDesconto() : 0.0;
        double valorDesconto = valorTotal * (descontoPercentual / 100);
        orcamento.setDesconto(descontoPercentual);
        orcamento.setValorFinal(valorTotal - valorDesconto);

        return orcamentoRepository.save(orcamento);
    }

    /**
     * Exclui um orçamento do sistema com base no seu ID.
     *
     * @param id O ID do orçamento a ser excluído.
     */
    @Transactional
    public void excluirOrcamento(Long id) {
        orcamentoRepository.deleteById(id);
    }
}