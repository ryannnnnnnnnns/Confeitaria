package com.ifsp.projeto.repository;

import com.ifsp.projeto.model.Produto;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    @Query("SELECT p FROM Produto p LEFT JOIN FETCH p.ingredientes i LEFT JOIN FETCH i.materiaPrima WHERE p.id = :id")
    Optional<Produto> findByIdWithIngredientes(@Param("id") Long id);

    List<Produto> findByNomeContainingIgnoreCase(String nome);
}
