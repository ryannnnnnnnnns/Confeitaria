package com.ifsp.projeto.repository;

import com.ifsp.projeto.model.Ingrediente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {
    boolean existsByMateriaPrimaId(Long materiaPrimaId);
}
