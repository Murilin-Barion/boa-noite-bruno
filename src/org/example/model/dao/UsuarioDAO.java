package org.example.model.dao;

import org.example.model.Usuario;

public interface UsuarioDAO extends GenericDAO<Usuario, Long> {
    Usuario findByEmailAndPassword(String email, String senha);
    Usuario findByEmail(String email);
}