package com.ifsp.projeto.repository;

import com.ifsp.projeto.model.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ItemPedido ip WHERE ip.produto.id = :produtoId")
    void deleteByProdutoId(@Param("produtoId") Long produtoId);
}
