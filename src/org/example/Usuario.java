package org.example;

import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private String nome;
    private String email;
    private String senha;
    private List<Transacao> transacoes;

    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.transacoes = new ArrayList<>();
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public boolean verificarSenha(String senha) {
        return this.senha.equals(senha);
    }

    public void adicionarTransacao(Transacao transacao) {
        transacoes.add(transacao);
    }

    public List<Transacao> getTransacoes() {
        return transacoes;
    }

    public double getTotalTransacoes(){
        double saldo = 0;

        for (Transacao transacao : transacoes) {
            if (transacao.getTipo().equalsIgnoreCase("Receita")) {
                saldo += transacao.getValor();
            } else {
                saldo -= transacao.getValor();
            }
        }

        return saldo;
    }

    public double getTotalReceitas(){
        double saldo = 0;

        for (Transacao transacao : transacoes) {
            if (transacao.getTipo().equalsIgnoreCase("Receita")) {
                saldo += transacao.getValor();
            }
        }

        return saldo;
    }

    public double getTotalDespesas(){
        double saldo = 0;

        for (Transacao transacao : transacoes) {
            if (transacao.getTipo().equalsIgnoreCase("Despesa")) {
                saldo += transacao.getValor();
            }
        }

        return saldo;
    }
}
