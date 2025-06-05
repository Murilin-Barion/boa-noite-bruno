package org.example.model.dao.impl;

import org.example.model.Categoria;
import org.example.model.HibernateUtil;
import org.example.model.Transacao;
import org.example.model.Usuario;
import org.example.model.dao.TransacaoDAO;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransacaoDAOImpl implements TransacaoDAO {

    private Session session;

    public TransacaoDAOImpl(Session session) {
        this.session = session;
    }

    @Override
    public Transacao save(Transacao transacao) {
        // Transaction transaction = null; // Removido
        try {
            // transaction = session.beginTransaction(); // Removido
            Transacao mergedTransacao = session.merge(transacao);
            // transaction.commit(); // Removido
            return mergedTransacao;
        } catch (Exception e) {
            // if (transaction != null && transaction.isActive()) { // Removido
            //     transaction.rollback(); // Removido
            // }
            System.err.println("Erro ao salvar/atualizar transação: " + e.getMessage());
            e.printStackTrace();
            // Lançar exceção para o Controller tratar o rollback
            throw new RuntimeException("Erro ao salvar/atualizar transação", e);
        }
    }

    @Override
    public Transacao findById(Long id) {
        try {
            return session.find(Transacao.class, id);
        } catch (Exception e) {
            System.err.println("Erro ao buscar transação por ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Transacao> findAll() {
        try {
            return session.createQuery("FROM Transacao", Transacao.class).list();
        } catch (Exception e) {
            System.err.println("Erro ao buscar todas as transações: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // Updated delete method to accept the entity object
    @Override
    public void delete(Transacao transacao) {
        // Transaction transaction = null; // Removido
        try {
            // transaction = session.beginTransaction(); // Removido
            if (transacao != null && session.contains(transacao)) {
                 session.remove(transacao);
                 // transaction.commit(); // Removido
            } else if (transacao != null) {
                 // If the entity is detached, merge it first
                 Transacao managedTransacao = session.merge(transacao);
                 session.remove(managedTransacao);
                 // transaction.commit(); // Removido
            } else {
                 // if (transaction != null) transaction.rollback(); // Removido
                 System.err.println("Tentativa de excluir uma transação nula.");
                 throw new IllegalArgumentException("Tentativa de excluir uma transação nula.");
            }
        } catch (Exception e) {
            // if (transaction != null && transaction.isActive()) { // Removido
            //     transaction.rollback(); // Removido
            // }
            System.err.println("Erro ao deletar transação: " + e.getMessage());
            e.printStackTrace();
            // Lançar exceção para o Controller tratar o rollback
            throw new RuntimeException("Erro ao deletar transação", e);
        }
    }

    @Override
    public List<Transacao> findByUsuario(Usuario usuario) {
         try {
            Query<Transacao> query = session.createQuery(
                    "FROM Transacao t WHERE t.usuario = :usuario ORDER BY t.data DESC, t.id DESC", Transacao.class);
            query.setParameter("usuario", usuario);
            return query.list();
        } catch (Exception e) {
            System.err.println("Erro ao buscar transações por usuário: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public List<Transacao> findByCriteria(Usuario usuario, LocalDate startDate, LocalDate endDate, String tipo, Categoria categoria) {
        try {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Transacao> cq = cb.createQuery(Transacao.class);
            Root<Transacao> root = cq.from(Transacao.class);
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("usuario"), usuario));

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("data"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("data"), endDate));
            }
            if (tipo != null && !tipo.equalsIgnoreCase("Todos")) {
                predicates.add(cb.equal(root.get("tipo"), tipo));
            }
            if (categoria != null) {
                predicates.add(cb.equal(root.get("categoria"), categoria));
            }

            cq.where(predicates.toArray(new Predicate[0]));
            cq.orderBy(cb.desc(root.get("data")), cb.desc(root.get("id")));

            return session.createQuery(cq).getResultList();
        } catch (Exception e) {
            System.err.println("Erro ao buscar transações por critérios: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public long countByCategoria(Categoria categoria) {
        try {
            Query<Long> query = session.createQuery(
                    "SELECT count(t) FROM Transacao t WHERE t.categoria = :categoria", Long.class);
            query.setParameter("categoria", categoria);
            return query.uniqueResultOptional().orElse(0L);
        } catch (Exception e) {
            System.err.println("Erro ao contar transações por categoria: " + e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }
}

