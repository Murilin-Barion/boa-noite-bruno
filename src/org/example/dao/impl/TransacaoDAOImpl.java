package org.example.dao.impl;

import org.example.Transacao;
import org.example.Usuario;
import org.example.dao.TransacaoDAO;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.time.LocalDate;
import java.util.List;

public class TransacaoDAOImpl implements TransacaoDAO {

    private Session session;

    public TransacaoDAOImpl(Session session) {
        this.session = session;
    }

    @Override
    public Transacao save(Transacao transacao) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.merge(transacao);
            transaction.commit();
            return transacao;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            System.err.println("Erro ao salvar/atualizar transação: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Transacao findById(Long id) {
        return session.find(Transacao.class, id);
    }

    @Override
    public List<Transacao> findAll() {
        return session.createQuery("FROM Transacao", Transacao.class).list();
    }

    @Override
    public void delete(Long id) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Transacao transacao = session.find(Transacao.class, id);
            if (transacao != null) {
                session.remove(transacao);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            System.err.println("Erro ao deletar transação: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Transacao> findByUsuario(Usuario usuario) {
        Query<Transacao> query = session.createQuery(
                "FROM Transacao t WHERE t.usuario = :usuario ORDER BY t.data DESC", Transacao.class);
        query.setParameter("usuario", usuario);
        return query.list();
    }

    @Override
    public List<Transacao> findByUsuarioAndFilters(Usuario usuario, LocalDate startDate, LocalDate endDate, String type, String categoryDesc) {
        StringBuilder hql = new StringBuilder("FROM Transacao t WHERE t.usuario = :usuario");
        if (startDate != null) {
            hql.append(" AND t.data >= :startDate");
        }
        if (endDate != null) {
            hql.append(" AND t.data <= :endDate");
        }
        if (type != null && !type.equalsIgnoreCase("Todos")) {
            hql.append(" AND t.tipo = :type");
        }
        if (categoryDesc != null && !categoryDesc.equalsIgnoreCase("Todas")) {
            hql.append(" AND t.categoria.descricao = :categoryDesc");
        }
        hql.append(" ORDER BY t.data DESC");

        Query<Transacao> query = session.createQuery(hql.toString(), Transacao.class);
        query.setParameter("usuario", usuario);
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }
        if (endDate != null) {
            query.setParameter("endDate", endDate);
        }
        if (type != null && !type.equalsIgnoreCase("Todos")) {
            query.setParameter("type", type);
        }
        if (categoryDesc != null && !categoryDesc.equalsIgnoreCase("Todas")) {
            query.setParameter("categoryDesc", categoryDesc);
        }

        return query.list();
    }
}