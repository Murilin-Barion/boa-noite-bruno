package org.example.model.dao.impl;

import org.example.model.Categoria;
import org.example.model.HibernateUtil;
import org.example.model.Usuario;
import org.example.model.dao.CategoriaDAO;
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
        // Transaction transaction = null; // Removido
        try {
            // transaction = session.beginTransaction(); // Removido
            Categoria mergedCategoria = session.merge(categoria);
            // transaction.commit(); // Removido
            return mergedCategoria;
        } catch (Exception e) {
            // if (transaction != null && transaction.isActive()) { // Removido
            //     transaction.rollback(); // Removido
            // }
            System.err.println("Erro ao salvar categoria: " + e.getMessage());
            e.printStackTrace();
            // Lançar exceção para o Controller tratar o rollback
            throw new RuntimeException("Erro ao salvar categoria", e);
        }
    }

    @Override
    public Categoria update(Categoria categoria) {
         // Transaction transaction = null; // Removido
        try {
            // transaction = session.beginTransaction(); // Removido
            Categoria updatedCategoria = session.merge(categoria);
            // transaction.commit(); // Removido
            return updatedCategoria;
        } catch (Exception e) {
            // if (transaction != null && transaction.isActive()) { // Removido
            //     transaction.rollback(); // Removido
            // }
            System.err.println("Erro ao atualizar categoria: " + e.getMessage());
            e.printStackTrace();
            // Lançar exceção para o Controller tratar o rollback
            throw new RuntimeException("Erro ao atualizar categoria", e);
        }
    }

    @Override
    public Categoria findById(Long id) {
        try {
            return session.find(Categoria.class, id);
        } catch (Exception e) {
            System.err.println("Erro ao buscar categoria por ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Categoria> findAll() {
        try {
            return session.createQuery("FROM Categoria", Categoria.class).list();
        } catch (Exception e) {
            System.err.println("Erro ao buscar todas as categorias: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // Updated delete method to accept the entity object
    @Override
    public void delete(Categoria categoria) {
        // Transaction transaction = null; // Removido
        try {
            // transaction = session.beginTransaction(); // Removido
            if (categoria != null && session.contains(categoria)) {
                 session.remove(categoria);
                 // transaction.commit(); // Removido
            } else if (categoria != null) {
                 // If the entity is detached, merge it first
                 Categoria managedCategoria = session.merge(categoria);
                 session.remove(managedCategoria);
                 // transaction.commit(); // Removido
            } else {
                 // if (transaction != null) transaction.rollback(); // Removido
                 System.err.println("Tentativa de excluir uma categoria nula.");
                 throw new IllegalArgumentException("Tentativa de excluir uma categoria nula.");
            }
        } catch (Exception e) {
            // if (transaction != null && transaction.isActive()) { // Removido
            //     transaction.rollback(); // Removido
            // }
            System.err.println("Erro ao deletar categoria: " + e.getMessage());
            e.printStackTrace();
            // Lançar exceção para o Controller tratar o rollback
            throw new RuntimeException("Erro ao deletar categoria", e);
        }
    }

    @Override
    public List<Categoria> findByUsuario(Usuario usuario) {
        try {
            Query<Categoria> query = session.createQuery(
                    "FROM Categoria c WHERE c.usuario = :usuario ORDER BY c.descricao", Categoria.class);
            query.setParameter("usuario", usuario);
            return query.list();
        } catch (Exception e) {
            System.err.println("Erro ao buscar categorias por usuário: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public Categoria findByUsuarioAndDescricao(Usuario usuario, String descricao) {
        try {
            Query<Categoria> query = session.createQuery(
                    "FROM Categoria c WHERE c.usuario = :usuario AND lower(c.descricao) = lower(:descricao)", Categoria.class);
            query.setParameter("usuario", usuario);
            query.setParameter("descricao", descricao);
            List<Categoria> results = query.list();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            System.err.println("Erro ao buscar categoria por usuário e descrição: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

