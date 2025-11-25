package com.ifsp.projeto.controller.dto;

import com.ifsp.projeto.model.ItemVenda;

public class ItemVendaDTO {
    private Long producaoId;
    private int quantidade;
    private double valorUnitario;

    public ItemVendaDTO() {
    }

    public ItemVendaDTO(ItemVenda item) {
        this.producaoId = item.getProducao().getId();
        this.quantidade = item.getQuantidade();
        this.valorUnitario = item.getValorUnitario();
    }

    public Long getProducaoId() {
        return producaoId;
    }


    public void setProducaoId(Long producaoId) {
        this.producaoId = producaoId;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public double getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(double valorUnitario) {
        this.valorUnitario = valorUnitario;
    }
}
