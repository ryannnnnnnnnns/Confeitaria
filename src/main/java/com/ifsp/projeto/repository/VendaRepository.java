package com.ifsp.projeto.repository;

import com.ifsp.projeto.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VendaRepository extends JpaRepository<Venda, Long> {
    List<Venda> findByDataVenda(LocalDate dataVenda);

    @Query("SELECT DISTINCT v FROM Venda v LEFT JOIN FETCH v.itens iv LEFT JOIN FETCH iv.producao p LEFT JOIN FETCH p.produto WHERE v.dataVenda = :data")
    List<Venda> findByDataVendaWithProducaoAndProduto(@Param("data") LocalDate data);

    @Query("SELECT DISTINCT v FROM Venda v LEFT JOIN FETCH v.itens iv LEFT JOIN FETCH iv.producao p LEFT JOIN FETCH p.produto")
    List<Venda> findAllWithProducaoAndProduto();

    @Query("SELECT DISTINCT v FROM Venda v LEFT JOIN FETCH v.itens iv LEFT JOIN FETCH iv.producao p LEFT JOIN FETCH p.produto ORDER BY v.dataVenda DESC")
    List<Venda> findAllWithDetailsOrderByDataVendaDesc();

    @Query("SELECT DISTINCT v FROM Venda v LEFT JOIN FETCH v.itens iv LEFT JOIN FETCH iv.producao p LEFT JOIN FETCH p.produto WHERE v.dataVenda BETWEEN :dataInicio AND :dataFim ORDER BY v.dataVenda DESC")
    List<Venda> findByDataVendaBetweenWithDetailsOrderByDataVendaDesc(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);
}