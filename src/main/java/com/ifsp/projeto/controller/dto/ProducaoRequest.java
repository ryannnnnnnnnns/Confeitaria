package com.ifsp.projeto.controller.dto;

import java.util.List;

public class ProducaoRequest {
    private List<ProducaoDTO> produtos;
    private String dataProducao;

    // Getters and Setters
    public List<ProducaoDTO> getProdutos() {
        return produtos;
    }

    public void setProdutos(List<ProducaoDTO> produtos) {
        this.produtos = produtos;
    }

    public String getDataProducao() {
        return dataProducao;
    }

    public void setDataProducao(String dataProducao) {
        this.dataProducao = dataProducao;
    }
}
