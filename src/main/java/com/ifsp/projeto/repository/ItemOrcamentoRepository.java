package com.ifsp.projeto.repository;

import com.ifsp.projeto.model.ItemOrcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemOrcamentoRepository extends JpaRepository<ItemOrcamento, Long> {
    @Modifying
    @Query("DELETE FROM ItemOrcamento io WHERE io.produto.id = :produtoId")
    void deleteByProdutoId(@Param("produtoId") Long produtoId);
}
