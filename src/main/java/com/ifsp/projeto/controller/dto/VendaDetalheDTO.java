package com.ifsp.projeto.controller.dto;

public record VendaDetalheDTO(Long id, String produtoNome, Double valorVenda, String formaPagamento, boolean isDoado) {}
