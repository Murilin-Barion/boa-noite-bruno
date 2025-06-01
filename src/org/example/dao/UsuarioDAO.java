package org.example.dao;

import org.example.Usuario;

public interface UsuarioDAO extends GenericDAO<Usuario, Long> {
    Usuario findByEmailAndPassword(String email, String senha);
    Usuario findByEmail(String email);
}