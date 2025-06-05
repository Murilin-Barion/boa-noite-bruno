package org.example;

import org.example.model.HibernateUtil;
import org.example.view.LoginScreen;

import javax.swing.*;
import org.hibernate.Session;

public class Main {
    public static void main(String[] args) {
        try {
            HibernateUtil.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Falha ao criar a SessionFactory inicial." + ex);
            JOptionPane.showMessageDialog(null, "Erro crítico ao inicializar a persistência.", "Erro Fatal", JOptionPane.ERROR_MESSAGE);
            throw new ExceptionInInitializerError(ex);
        }

        try (Session session = HibernateUtil.openSession()) {
            System.out.println("Conexão com o banco de dados SQLite estabelecida e tabelas criadas/atualizadas com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao conectar ou inicializar o banco de dados: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Não foi possível conectar ao banco de dados.", "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> {
            new LoginScreen().setVisible(true);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Fechando SessionFactory do Hibernate.");
            HibernateUtil.shutdown();
        }));
    }
}

