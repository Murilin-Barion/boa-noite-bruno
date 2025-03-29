package org.example;

public class Categoria {
    private String descricao;

    public Categoria(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Categoria categoria = (Categoria) obj;
        return descricao.equalsIgnoreCase(categoria.descricao);
    }

    @Override
    public int hashCode() {
        return descricao.toLowerCase().hashCode();
    }
}

