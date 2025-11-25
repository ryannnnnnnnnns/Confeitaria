package com.ifsp.projeto.controller.dto;

import com.ifsp.projeto.model.MateriaPrima;

public class MateriaPrimaComAlertaDTO {
    private final MateriaPrima materiaPrima;
    private final boolean lowStock;
    private final Double valorTotal;

    public MateriaPrimaComAlertaDTO(MateriaPrima materiaPrima, boolean isLowStock) {
        this.materiaPrima = materiaPrima;
        this.lowStock = isLowStock;
        if (materiaPrima.getQuantidade() != null && materiaPrima.getValor() != null) {
            this.valorTotal = materiaPrima.getQuantidade() * materiaPrima.getValor();
        } else {
            this.valorTotal = 0.0;
        }
    }

    public MateriaPrima getMateriaPrima() {
        return materiaPrima;
    }

    public boolean isLowStock() {
        return lowStock;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    // Adicionado para compatibilidade com Thymeleaf
    public boolean getLowStock() {
        return lowStock;
    }
}