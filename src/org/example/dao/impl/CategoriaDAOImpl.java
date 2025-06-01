package org.example.dao.impl;

import org.example.Categoria;
import org.example.Usuario;
import org.example.dao.CategoriaDAO;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

public class CategoriaDAOImpl implements CategoriaDAO {

    private Session session;

    public CategoriaDAOImpl(Session session) {
        this.session = session;
    }

    @Override
    public Categoria save(Categoria categoria) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.merge(categoria);
            transaction.commit();
            return categoria;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            System.err.println("Erro ao salvar/atualizar categoria: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Categoria findById(Long id) {
        return session.find(Categoria.class, id);
    }

    @Override
    public List<Categoria> findAll() {
        return session.createQuery("FROM Categoria", Categoria.class).list();
    }

    @Override
    public void delete(Long id) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Categoria categoria = session.find(Categoria.class, id);
            if (categoria != null) {
                session.remove(categoria);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            System.err.println("Erro ao deletar categoria: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Categoria> findByUsuario(Usuario usuario) {
        Query<Categoria> query = session.createQuery(
                "FROM Categoria c WHERE c.usuario = :usuario", Categoria.class);
        query.setParameter("usuario", usuario);
        return query.list();
    }

    @Override
    public Categoria findByUsuarioAndDescricao(Usuario usuario, String descricao) {
        Query<Categoria> query = session.createQuery(
                "FROM Categoria c WHERE c.usuario = :usuario AND lower(c.descricao) = lower(:descricao)", Categoria.class);
        query.setParameter("usuario", usuario);
        query.setParameter("descricao", descricao);
        return query.uniqueResult();
    }
}