package org.example.controller;

import org.example.model.HibernateUtil;
import org.example.model.Usuario;
import org.example.model.dao.UsuarioDAO;
import org.example.model.dao.impl.UsuarioDAOImpl;
import org.hibernate.Session;
import org.hibernate.Transaction; // Import Transaction

public class RegisterController {

    public boolean registerUser(String name, String email, String password) throws Exception {
        Session session = null;
        Transaction transaction = null; // Declare transaction
        boolean success = false;
        try {
            session = HibernateUtil.openSession();
            transaction = session.beginTransaction(); // Start transaction
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl(session);

            // Verifica se o email já existe
            if (usuarioDAO.findByEmail(email) != null) {
                // Não precisa de rollback aqui, pois nenhuma alteração foi feita ainda
                throw new Exception("Este e-mail já está cadastrado.");
            }

            Usuario novoUsuario = new Usuario(name, email, password);
            Usuario usuarioSalvo = usuarioDAO.save(novoUsuario); // DAO agora só faz o merge

            transaction.commit(); // Commit transaction after successful save

            if (usuarioSalvo != null && usuarioSalvo.getId() != null) {
                success = true;
            }
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback(); // Rollback transaction on error
            }
            // Logar a exceção seria uma boa prática aqui
            System.err.println("Erro ao tentar registrar usuário: " + e.getMessage());
            e.printStackTrace();
            // Re-lança a exceção para ser tratada pela view ou camada superior
            // Mantém a mensagem original se for a de e-mail duplicado
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