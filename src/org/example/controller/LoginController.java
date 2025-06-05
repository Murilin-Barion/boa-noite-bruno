package org.example.controller;

import org.example.model.HibernateUtil;
import org.example.model.Usuario;
import org.example.model.dao.UsuarioDAO;
import org.example.model.dao.impl.UsuarioDAOImpl;
import org.hibernate.Hibernate;
import org.hibernate.Session;

public class LoginController {

    public Usuario login(String email, String senha) throws Exception {
        Usuario usuario = null;
        Session session = null;
        try {
            session = HibernateUtil.openSession();
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl(session);
            usuario = usuarioDAO.findByEmailAndPassword(email, senha);

            if (usuario != null) {
                Hibernate.initialize(usuario.getTransacoes());
                Hibernate.initialize(usuario.getCategorias());
            }
        } catch (Exception e) {
            System.err.println("Erro ao tentar fazer login: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Erro ao processar login: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return usuario;
    }
}

