package com.ifsp.projeto.repository;

import com.ifsp.projeto.controller.dto.ProducaoDisponivelDTO;
import com.ifsp.projeto.model.Producao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProducaoRepository extends JpaRepository<Producao, Long> {
    @Query("SELECT p FROM Producao p JOIN FETCH p.produto WHERE p.dataProducao = :data")
    List<Producao> findByDataProducao(@Param("data") LocalDate data);
    boolean existsByProdutoId(Long id);

    List<Producao> findByProdutoId(Long produtoId);

    @Query("SELECT p FROM Producao p JOIN FETCH p.produto")
    List<Producao> findAllWithProduto();

    @Query("SELECT new com.ifsp.projeto.controller.dto.ProducaoDisponivelDTO(p.id, p.produto.nome, p.dataProducao, p.quantidade - COALESCE((SELECT SUM(iv.quantidade) FROM ItemVenda iv WHERE iv.producao = p), 0), p.produto.preco) FROM Producao p WHERE p.quantidade > COALESCE((SELECT SUM(iv.quantidade) FROM ItemVenda iv WHERE iv.producao = p), 0)")
    List<ProducaoDisponivelDTO> findProducoesComEstoqueDisponivel();

    @Query("SELECT new com.ifsp.projeto.controller.dto.ProducaoDisponivelDTO(p.id, p.produto.nome, p.dataProducao, p.quantidade - COALESCE((SELECT SUM(iv.quantidade) FROM ItemVenda iv WHERE iv.producao = p AND iv.venda.id != :vendaId), 0), p.produto.preco) FROM Producao p WHERE p.quantidade > COALESCE((SELECT SUM(iv.quantidade) FROM ItemVenda iv WHERE iv.producao = p AND iv.venda.id != :vendaId), 0)")
    List<ProducaoDisponivelDTO> findProducoesComEstoqueDisponivelParaVenda(@Param("vendaId") Long vendaId);

}
