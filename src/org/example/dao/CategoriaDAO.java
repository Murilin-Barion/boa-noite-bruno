package org.example.dao;

import org.example.Categoria;
import org.example.Usuario;

import java.util.List;

public interface CategoriaDAO extends GenericDAO<Categoria, Long> {
    List<Categoria> findByUsuario(Usuario usuario);
    Categoria findByUsuarioAndDescricao(Usuario usuario, String descricao);
}