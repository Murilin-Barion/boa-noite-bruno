package org.example;

import javax.swing.*;
import org.hibernate.Session;

public class Main {
    public static void main(String[] args) {
        // 1. Inicializa a SessionFactory do Hibernate
        HibernateUtil.buildSessionFactory();

        // Teste de conexão e a criação do DB criado em ./data/finance_manager.db
        try (Session session = HibernateUtil.openSession()) {
            System.out.println("Conexão com o banco de dados SQLite estabelecida e tabelas criadas/atualizadas com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao conectar ou inicializar o banco de dados: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Não foi possível conectar ao banco de dados.", "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Inicia a interface gráfica Swing
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginScreen();
            }
        });

        // fechar a SessionFactory quando a aplicação for encerrada (pensa num trem dificil de lembrar)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Fechando SessionFactory do Hibernate.");
            HibernateUtil.shutdown();
        }));
    }
}