package com.ifsp.projeto.controller.dto;

import jakarta.validation.constraints.NotNull;

public class ItemOrcamentoDTO {
    private Long produtoId;
    private int quantidade;
    @NotNull(message = "O valor unitário não pode ser nulo.")
    private Double valorUnitario;

    public Double getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(Double valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }
}
