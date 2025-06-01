package org.example.dao.impl;

import org.example.Usuario;
import org.example.dao.UsuarioDAO;
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
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.merge(usuario);
            transaction.commit();
            return usuario;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            System.err.println("Erro ao salvar/atualizar usuário: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Usuario findById(Long id) {
        return session.find(Usuario.class, id);
    }

    @Override
    public List<Usuario> findAll() {
        return session.createQuery("FROM Usuario", Usuario.class).list();
    }

    @Override
    public void delete(Long id) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Usuario usuario = session.find(Usuario.class, id);
            if (usuario != null) {
                session.remove(usuario);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            System.err.println("Erro ao deletar usuário: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Usuario findByEmailAndPassword(String email, String senha) {
        Query<Usuario> query = session.createQuery(
                "FROM Usuario u WHERE u.email = :email AND u.senha = :senha", Usuario.class);
        query.setParameter("email", email);
        query.setParameter("senha", senha);
        return query.uniqueResult();
    }

    @Override
    public Usuario findByEmail(String email) {
        Query<Usuario> query = session.createQuery(
                "FROM Usuario u WHERE u.email = :email", Usuario.class);
        query.setParameter("email", email);
        return query.uniqueResult();
    }
}