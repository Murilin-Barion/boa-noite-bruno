package org.example;

import java.time.LocalDate;

public class Transacao {
    private String tipo;
    private Categoria categoria;
    private LocalDate data;
    private String descricao;
    private double valor;

    public Transacao(String tipo, Categoria categoria, LocalDate data, String descricao, double valor) {
        this.tipo = tipo;
        this.categoria = categoria; // Inicalmente como string, mas depois como objeto
        this.data = data;
        this.descricao = descricao;
        this.valor = valor;
    }

    public String getTipo() {
        return tipo;
    }

    public double getValor() {
        return valor;
    }

    public LocalDate getData() {
        return data;
    }

    public String getDescricao() {
        return descricao;
    }

    public Categoria getCategoria() {
        return categoria;
    }
}
