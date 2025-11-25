package com.ifsp.projeto.repository;

import com.ifsp.projeto.model.ItemVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemVendaRepository extends JpaRepository<ItemVenda, Long> {
    @Query("SELECT COALESCE(SUM(iv.quantidade), 0) FROM ItemVenda iv WHERE iv.producao.id = :producaoId")
    long sumQuantidadeByProducaoId(@Param("producaoId") Long producaoId);

    @Query("SELECT COALESCE(SUM(iv.quantidade), 0) FROM ItemVenda iv WHERE iv.producao.id = :producaoId AND iv.venda.id != :vendaId")
    long sumQuantidadeByProducaoIdAndVendaIdNot(@Param("producaoId") Long producaoId, @Param("vendaId") Long vendaId);

    @Modifying
    @Query("DELETE FROM ItemVenda iv WHERE iv.producao.id = :producaoId")
    void deleteByProducaoId(@Param("producaoId") Long producaoId);
}