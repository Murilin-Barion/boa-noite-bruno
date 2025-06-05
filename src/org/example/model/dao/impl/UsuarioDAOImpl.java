package org.example.model.dao.impl;

import org.example.model.HibernateUtil;
import org.example.model.Usuario;
import org.example.model.dao.UsuarioDAO;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class UsuarioDAOImpl implements UsuarioDAO {

    private Session session;

    public UsuarioDAOImpl(Session session) {
        this.session = session;
    }

    @Override
    public Usuario save(Usuario usuario) {
        // Transaction transaction = null; // Removido - Gerenciado pelo Controller
        try {
            // transaction = session.beginTransaction(); // Removido
            // Use merge for save/update
            Usuario mergedUsuario = session.merge(usuario);
            // transaction.commit(); // Removido
            return mergedUsuario;
        } catch (Exception e) {
            // if (transaction != null && transaction.isActive()) { // Removido
            //     transaction.rollback(); // Removido
            // }
            System.err.println("Erro ao salvar/atualizar usuário: " + e.getMessage());
            e.printStackTrace();
            // Lançar exceção para o Controller tratar o rollback
            throw new RuntimeException("Erro ao salvar/atualizar usuário", e);
        }
    }

    @Override
    public Usuario findById(Long id) {
         try {
            return session.find(Usuario.class, id);
        } catch (Exception e) {
            System.err.println("Erro ao buscar usuário por ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Usuario> findAll() {
         try {
            return session.createQuery("FROM Usuario", Usuario.class).list();
        } catch (Exception e) {
            System.err.println("Erro ao buscar todos os usuários: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // Updated delete method to accept the entity object
    @Override
    public void delete(Usuario usuario) {
        // Transaction transaction = null; // Removido
        try {
            // transaction = session.beginTransaction(); // Removido
            if (usuario != null && session.contains(usuario)) {
                 session.remove(usuario);
                 // transaction.commit(); // Removido
            } else if (usuario != null) {
                 // If the entity is detached, merge it first to make it managed
                 Usuario managedUsuario = session.merge(usuario);
                 session.remove(managedUsuario);
                 // transaction.commit(); // Removido
            } else {
                 // if (transaction != null) transaction.rollback(); // Removido
                 System.err.println("Tentativa de excluir um usuário nulo.");
                 // Lançar exceção ou tratar como erro
                 throw new IllegalArgumentException("Tentativa de excluir um usuário nulo.");
            }
        } catch (Exception e) {
            // if (transaction != null && transaction.isActive()) { // Removido
            //     transaction.rollback(); // Removido
            // }
            System.err.println("Erro ao deletar usuário: " + e.getMessage());
            e.printStackTrace();
            // Lançar exceção para o Controller tratar o rollback
            throw new RuntimeException("Erro ao deletar usuário", e);
        }
    }

    @Override
    public Usuario findByEmailAndPassword(String email, String senha) {
        try {
            Query<Usuario> query = session.createQuery(
                    "FROM Usuario u WHERE u.email = :email AND u.senha = :senha", Usuario.class);
            query.setParameter("email", email);
            query.setParameter("senha", senha);
            // Use list() and check size to avoid exceptions
            List<Usuario> results = query.list();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            System.err.println("Erro ao buscar usuário por email e senha: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Usuario findByEmail(String email) {
         try {
            Query<Usuario> query = session.createQuery(
                    "FROM Usuario u WHERE u.email = :email", Usuario.class);
            query.setParameter("email", email);
            // Use list() and check size
            List<Usuario> results = query.list();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            System.err.println("Erro ao buscar usuário por email: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

