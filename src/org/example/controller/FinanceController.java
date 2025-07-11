package org.example.controller;

import org.example.model.*;
import org.example.model.dao.CategoriaDAO;
import org.example.model.dao.TransacaoDAO;
import org.example.model.dao.UsuarioDAO;
import org.example.model.dao.impl.CategoriaDAOImpl;
import org.example.model.dao.impl.TransacaoDAOImpl;
import org.example.model.dao.impl.UsuarioDAOImpl;
import org.hibernate.Hibernate;
import org.hibernate.Session;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FinanceController {

    private Usuario currentUser;

    public FinanceController(Usuario user) {
        this.currentUser = loadFullUserData(user.getId());
    }

    private Usuario loadFullUserData(Long userId) {
        Session session = null;
        try {
            session = HibernateUtil.openSession();
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl(session);
            Usuario user = usuarioDAO.findById(userId);
            if (user != null) {
                Hibernate.initialize(user.getTransacoes());
                Hibernate.initialize(user.getCategorias());
            }
            return user;
        } catch (Exception e) {
            System.err.println("Erro ao carregar dados completos do usuário: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public Usuario getCurrentUser() {
        return currentUser;
    }

    public List<Categoria> getCategoriasDoUsuario() {
        if (currentUser != null && currentUser.getCategorias() != null) {
            return new ArrayList<>(currentUser.getCategorias());
        }
        return new ArrayList<>();
    }

    public List<Transacao> getTransacoesDoUsuario() {
        if (currentUser != null && currentUser.getTransacoes() != null) {
            return new ArrayList<>(currentUser.getTransacoes());
        }
        return new ArrayList<>();
    }

    public Transacao addTransaction(String tipo, String categoriaDesc, LocalDate data, String descricao, double valor) throws Exception {
        Session session = null;
        Transacao savedTransaction = null;
        try {
            session = HibernateUtil.openSession();
            session.beginTransaction();

            CategoriaDAO categoriaDAO = new CategoriaDAOImpl(session);
            TransacaoDAO transacaoDAO = new TransacaoDAOImpl(session);
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl(session);

            Categoria categoria = categoriaDAO.findByUsuarioAndDescricao(currentUser, categoriaDesc);
            if (categoria == null) {
                throw new Exception("Categoria selecionada não encontrada.");
            }

            Transacao transaction = new Transacao(tipo, categoria, data, descricao, valor, currentUser);
            savedTransaction = transacaoDAO.save(transaction);

            this.currentUser = usuarioDAO.findById(currentUser.getId());
            Hibernate.initialize(currentUser.getTransacoes());
            Hibernate.initialize(currentUser.getCategorias());

            session.getTransaction().commit();
        } catch (Exception e) {
            if (session != null && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            System.err.println("Erro ao adicionar transação: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Erro ao adicionar transação: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return savedTransaction;
    }

    public Categoria addCategory(String novaCategoriaDesc) throws Exception {
        Session session = null;
        Categoria savedCategoria = null;
        try {
            session = HibernateUtil.openSession();
            session.beginTransaction();
            CategoriaDAO categoriaDAO = new CategoriaDAOImpl(session);
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl(session);

            if (categoriaDAO.findByUsuarioAndDescricao(currentUser, novaCategoriaDesc.trim()) != null) {
                throw new Exception("Categoria com este nome já existe!");
            }

            Categoria novaCategoria = new Categoria(novaCategoriaDesc.trim(), currentUser);
            savedCategoria = categoriaDAO.save(novaCategoria);

            this.currentUser = usuarioDAO.findById(currentUser.getId());
            Hibernate.initialize(currentUser.getCategorias());
            Hibernate.initialize(currentUser.getTransacoes());

            session.getTransaction().commit();
        } catch (Exception e) {
            if (session != null && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            System.err.println("Erro ao adicionar categoria: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Erro ao adicionar categoria: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return savedCategoria;
    }

    public Categoria editCategory(Long categoriaId, String novoNome) throws Exception {
        Session session = null;
        Categoria updatedCategoria = null;
        try {
            session = HibernateUtil.openSession();
            session.beginTransaction();
            CategoriaDAO categoriaDAO = new CategoriaDAOImpl(session);
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl(session);

            Categoria existenteComNovoNome = categoriaDAO.findByUsuarioAndDescricao(currentUser, novoNome.trim());
            if (existenteComNovoNome != null && !existenteComNovoNome.getId().equals(categoriaId)) {
                throw new Exception("Já existe uma categoria com o nome '" + novoNome.trim() + "'.");
            }

            Categoria categoriaParaEditar = categoriaDAO.findById(categoriaId);
            if (categoriaParaEditar == null || !categoriaParaEditar.getUsuario().getId().equals(currentUser.getId())) {
                throw new Exception("Categoria não encontrada ou não pertence a este usuário.");
            }

            categoriaParaEditar.setDescricao(novoNome.trim());
            updatedCategoria = categoriaDAO.update(categoriaParaEditar);

            this.currentUser = usuarioDAO.findById(currentUser.getId());
            Hibernate.initialize(currentUser.getCategorias());
            Hibernate.initialize(currentUser.getTransacoes());

            session.getTransaction().commit();
        } catch (Exception e) {
            if (session != null && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            System.err.println("Erro ao editar categoria: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Erro ao editar categoria: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return updatedCategoria;
    }

    public void deleteCategory(Long categoriaId) throws Exception {
        Session session = null;
        try {
            session = HibernateUtil.openSession();
            session.beginTransaction();
            CategoriaDAO categoriaDAO = new CategoriaDAOImpl(session);
            TransacaoDAO transacaoDAO = new TransacaoDAOImpl(session);
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl(session);

            Categoria categoriaParaExcluir = categoriaDAO.findById(categoriaId);
            if (categoriaParaExcluir == null || !categoriaParaExcluir.getUsuario().getId().equals(currentUser.getId())) {
                throw new Exception("Categoria não encontrada ou não pertence a este usuário.");
            }

            long count = transacaoDAO.countByCategoria(categoriaParaExcluir);
            if (count > 0) {
                throw new Exception("Não é possível excluir a categoria pois existem transações associadas a ela.");
            }

            categoriaDAO.delete(categoriaParaExcluir);

            this.currentUser = usuarioDAO.findById(currentUser.getId());
            Hibernate.initialize(currentUser.getCategorias());
            Hibernate.initialize(currentUser.getTransacoes());

            session.getTransaction().commit();
        } catch (Exception e) {
            if (session != null && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            System.err.println("Erro ao excluir categoria: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Erro ao excluir categoria: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public List<Transacao> filterTransactions(LocalDate startDate, LocalDate endDate, String tipo, String categoriaDesc) {
        Session session = null;
        try {
            session = HibernateUtil.openSession();
            TransacaoDAO transacaoDAO = new TransacaoDAOImpl(session);
            CategoriaDAO categoriaDAO = new CategoriaDAOImpl(session);

            Categoria categoria = null;
            if (categoriaDesc != null && !categoriaDesc.equalsIgnoreCase("Todas")) {
                categoria = categoriaDAO.findByUsuarioAndDescricao(currentUser, categoriaDesc);
            }

            return transacaoDAO.findByCriteria(currentUser, startDate, endDate, tipo, categoria);

        } catch (Exception e) {
            System.err.println("Erro ao filtrar transações: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public FinancialSummary calculateSummary(List<Transacao> transactions) {
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transacao t : transactions) {
            if ("Receita".equalsIgnoreCase(t.getTipo())) {
                totalIncome += t.getValor();
            } else if ("Despesa".equalsIgnoreCase(t.getTipo())) {
                totalExpense += t.getValor();
            }
        }
        double balance = totalIncome - totalExpense;
        return new FinancialSummary(balance, totalIncome, totalExpense);
    }

    public static class FinancialSummary {
        public final double balance;
        public final double totalIncome;
        public final double totalExpense;

        public FinancialSummary(double balance, double totalIncome, double totalExpense) {
            this.balance = balance;
            this.totalIncome = totalIncome;
            this.totalExpense = totalExpense;
        }
    }
}

