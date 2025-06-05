package org.example.controller;

import org.example.model.HibernateUtil;
import org.example.model.Usuario;
import org.example.model.dao.UsuarioDAO;
import org.example.model.dao.impl.UsuarioDAOImpl;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class RegisterController {

    public boolean registerUser(String name, String email, String password) throws Exception {
        Session session = null;
        Transaction transaction = null;
        boolean success = false;
        try {
            session = HibernateUtil.openSession();
            transaction = session.beginTransaction();
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl(session);

            if (usuarioDAO.findByEmail(email) != null) {
                throw new Exception("Este e-mail já está cadastrado.");
            }

            Usuario novoUsuario = new Usuario(name, email, password);
            Usuario usuarioSalvo = usuarioDAO.save(novoUsuario);

            transaction.commit();

            if (usuarioSalvo != null && usuarioSalvo.getId() != null) {
                success = true;
            }
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            System.err.println("Erro ao tentar registrar usuário: " + e.getMessage());
            e.printStackTrace();
            if (e.getMessage().contains("Este e-mail já está cadastrado.")) {
                throw e;
            } else {
                throw new Exception("Erro ao processar registro: " + e.getMessage(), e);
            }
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return success;
    }
}