package org.example.controller;

import org.example.model.HibernateUtil;
import org.example.model.Usuario;
import org.example.model.dao.UsuarioDAO;
import org.example.model.dao.impl.UsuarioDAOImpl;
import org.hibernate.Session;

public class RegisterController {

    public boolean registerUser(String name, String email, String password) throws Exception {
        Session session = null;
        boolean success = false;
        try {
            session = HibernateUtil.openSession();
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl(session);

            // Verifica se o email já existe
            if (usuarioDAO.findByEmail(email) != null) {
                throw new Exception("Este e-mail já está cadastrado.");
            }

            Usuario novoUsuario = new Usuario(name, email, password);
            Usuario usuarioSalvo = usuarioDAO.save(novoUsuario);

            if (usuarioSalvo != null && usuarioSalvo.getId() != null) {
                success = true;
            }
        } catch (Exception e) {
            // Logar a exceção seria uma boa prática aqui
            System.err.println("Erro ao tentar registrar usuário: " + e.getMessage());
            e.printStackTrace();
            // Re-lança a exceção para ser tratada pela view ou camada superior
            throw new Exception("Erro ao processar registro: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return success;
    }
}

