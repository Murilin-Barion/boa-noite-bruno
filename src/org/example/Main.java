package org.example;

import org.example.model.HibernateUtil; // Atualizado para o pacote model
import org.example.view.LoginScreen; // Atualizado para o pacote view

import javax.swing.*;
import org.hibernate.Session;

public class Main {
    public static void main(String[] args) {
        // 1. Inicializa a SessionFactory do Hibernate
        // A inicialização agora pode ser feita dentro do HibernateUtil ou mantida aqui
        // Vamos manter aqui para garantir que seja feita antes de qualquer operação
        try {
            HibernateUtil.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Falha ao criar a SessionFactory inicial." + ex);
            JOptionPane.showMessageDialog(null, "Erro crítico ao inicializar a persistência.", "Erro Fatal", JOptionPane.ERROR_MESSAGE);
            throw new ExceptionInInitializerError(ex);
        }

        // Teste de conexão (opcional, mas útil para feedback inicial)
        try (Session session = HibernateUtil.openSession()) {
            System.out.println("Conexão com o banco de dados SQLite estabelecida e tabelas criadas/atualizadas com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao conectar ou inicializar o banco de dados: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Não foi possível conectar ao banco de dados.", "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
            // Considerar se deve sair ou tentar continuar
            // System.exit(1);
        }

        // Inicia a interface gráfica Swing na Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Cria e exibe a tela de login, que agora está no pacote view
            new LoginScreen().setVisible(true);
        });

        // Adiciona um shutdown hook para fechar a SessionFactory corretamente
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Fechando SessionFactory do Hibernate.");
            HibernateUtil.shutdown();
        }));
    }
}

