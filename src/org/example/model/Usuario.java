package org.example.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String senha;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Transacao> transacoes = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Categoria> categorias = new ArrayList<>();

    // Construtor padrão
    public Usuario() {
    }

    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    // --- Getters e Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public List<Transacao> getTransacoes() { return transacoes; }
    public void setTransacoes(List<Transacao> transacoes) { this.transacoes = transacoes; }
    public List<Categoria> getCategorias() { return categorias; }
    public void setCategorias(List<Categoria> categorias) { this.categorias = categorias; }


    public boolean verificarSenha(String senha) {
        return this.senha.equals(senha);
    }

    public void adicionarTransacao(Transacao transacao) {
        this.transacoes.add(transacao);
        transacao.setUsuario(this); // Garante a ligação bidirecional
    }

    public boolean adicionarCategoria(Categoria categoria) {
        if (categoria == null || categoria.getDescricao() == null || categoria.getDescricao().trim().isEmpty()) {
            return false;
        }
        for (Categoria cat : this.categorias) {
            if (cat.getDescricao().equalsIgnoreCase(categoria.getDescricao())) {
                return false;
            }
        }
        this.categorias.add(categoria);
        categoria.setUsuario(this); // Garante a ligação bidirecional
        return true;
    }

    public boolean editarCategoria(int index, String novaDescricao) {
        if (index < 0 || index >= categorias.size() || novaDescricao == null || novaDescricao.trim().isEmpty()) {
            return false;
        }
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
        Categoria categoriaParaRemover = categorias.get(index);
        categorias.remove(index);
        categoriaParaRemover.setUsuario(null);
        return true;
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
        double receitas = 0;
        for (Transacao transacao : transacoes) {
            if (transacao.getTipo().equalsIgnoreCase("Receita")) {
                receitas += transacao.getValor();
            }
        }
        return receitas;
    }

    public double getTotalDespesas(){
        double despesas = 0;
        for (Transacao transacao : transacoes) {
            if (transacao.getTipo().equalsIgnoreCase("Despesa")) {
                despesas += transacao.getValor();
            }
        }
        return despesas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id) && Objects.equals(email, usuario.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}