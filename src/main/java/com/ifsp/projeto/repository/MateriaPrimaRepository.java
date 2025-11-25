package com.ifsp.projeto.repository;

import com.ifsp.projeto.model.MateriaPrima;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MateriaPrimaRepository extends JpaRepository<MateriaPrima, Long> {
    Optional<MateriaPrima> findByNomeAndUnidade(String nome, String unidade);
    List<MateriaPrima> findByNomeContainingIgnoreCase(String nome);
}
