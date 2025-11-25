package com.ifsp.projeto.controller.dto;

import java.time.LocalDate;

public class ProducaoDisponivelDTO {
    private Long producaoId;
    private String nomeProduto;
    private LocalDate dataProducao;
    private long quantidadeDisponivel;

    private Double preco;

    public ProducaoDisponivelDTO(Long producaoId, String nomeProduto, LocalDate dataProducao, long quantidadeDisponivel, Double preco) {
        this.producaoId = producaoId;
        this.nomeProduto = nomeProduto;
        this.dataProducao = dataProducao;
        this.quantidadeDisponivel = quantidadeDisponivel;
        this.preco = preco;
    }

    // Getters
    public Long getProducaoId() {
        return producaoId;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public LocalDate getDataProducao() {
        return dataProducao;
    }

    public long getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }

    public Double getPreco() {
        return preco;
    }
}
