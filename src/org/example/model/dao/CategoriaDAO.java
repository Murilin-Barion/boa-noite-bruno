package org.example.model.dao;

import org.example.model.Categoria;
import org.example.model.Usuario;

import java.util.List;

public interface CategoriaDAO extends GenericDAO<Categoria, Long> {
    List<Categoria> findByUsuario(Usuario usuario);
    Categoria findByUsuarioAndDescricao(Usuario usuario, String descricao);
    Categoria update(Categoria categoria);
}

