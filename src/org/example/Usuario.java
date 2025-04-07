package org.example;

import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private String nome;
    private String email;
    private String senha;
    private List<Transacao> transacoes;
    private List<Categoria> categorias;

    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.transacoes = new ArrayList<>();
        this.categorias = new ArrayList<>();
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

    public boolean adicionarCategoria(Categoria categoria) {
        if (categoria == null || categoria.getDescricao() == null || categoria.getDescricao().trim().isEmpty()) {
            return false;
        }

        // Verifica se já existe uma categoria com essa descrição (case insensitive)
        for (Categoria cat : categorias) {
            if (cat.getDescricao().equalsIgnoreCase(categoria.getDescricao())) {
                return false;
            }
        }

        categorias.add(categoria);
        return true;
    }

    public boolean editarCategoria(int index, String novaDescricao) {
        if (index < 0 || index >= categorias.size() || novaDescricao == null || novaDescricao.trim().isEmpty()) {
            return false;
        }

        // Verifica se já existe outra categoria com essa descrição
        for (int i = 0; i < categorias.size(); i++) {
            if (i != index && categorias.get(i).getDescricao().equalsIgnoreCase(novaDescricao)) {
                return false;
            }
        }

        categorias.get(index).setDescricao(novaDescricao);
        return true;
    }

    public boolean removerCategoria(int index) {
        if (index < 0 || index >= categorias.size()) {
            return false;
        }

        // Verifica se existem transações usando esta categoria
        for (Transacao transacao : transacoes) {
            if (transacao.getCategoria().equals(categorias.get(index))) {
                return false; // Não permite remover categoria em uso
            }
        }

        categorias.remove(index);
        return true;
    }

    public List<Categoria> getCategorias() {
        return categorias;
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
