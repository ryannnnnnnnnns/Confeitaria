package com.ifsp.projeto.controller.dto;

import com.ifsp.projeto.model.Venda;

import java.util.List;
import java.util.stream.Collectors;

public class VendaRequest {
    private List<ItemVendaDTO> itens;
    private String formaPagamento;
    private boolean doado;
    private String dataVenda;

    public VendaRequest() {
    }

    public VendaRequest(Venda venda) {
        this.dataVenda = venda.getDataVenda().toString();
        this.formaPagamento = venda.getFormaPagamento();
        this.doado = venda.isDoado();
        this.itens = venda.getItens().stream()
                .map(ItemVendaDTO::new)
                .collect(Collectors.toList());
    }

    public List<ItemVendaDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemVendaDTO> itens) {
        this.itens = itens;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public boolean isDoado() {
        return doado;
    }

    public void setDoado(boolean doado) {
        this.doado = doado;
    }

    public String getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(String dataVenda) {
        this.dataVenda = dataVenda;
    }
}
