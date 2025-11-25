package com.ifsp.projeto.repository;

import com.ifsp.projeto.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.time.LocalDate;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.itens WHERE p.id = :id")
    Pedido findByIdWithItens(Long id);

    @Query("SELECT p FROM Pedido p WHERE p.dataEntrega >= :data AND p.status <> 'Entregue' ORDER BY p.dataEntrega ASC")
    List<Pedido> findUpcomingPedidos(LocalDate data);

    @Query("SELECT p FROM Pedido p WHERE (:cliente IS NULL OR lower(p.cliente) LIKE lower(concat('%', :cliente, '%'))) AND (:status IS NULL OR p.status = :status) AND (:data IS NULL OR p.dataEntrega = :data)")
    List<Pedido> findWithFilters(@Param("cliente") String cliente, @Param("status") String status, @Param("data") LocalDate data);
}
