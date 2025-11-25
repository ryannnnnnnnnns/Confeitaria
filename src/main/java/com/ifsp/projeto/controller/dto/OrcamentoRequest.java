package com.ifsp.projeto.controller.dto;

import java.util.List;

public class OrcamentoRequest {
    private Long id;
    private String cliente;
    private List<ItemOrcamentoDTO> itens;
    private Double desconto;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public List<ItemOrcamentoDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemOrcamentoDTO> itens) {
        this.itens = itens;
    }

    public Double getDesconto() {
        return desconto;
    }

    public void setDesconto(Double desconto) {
        this.desconto = desconto;
    }
}
