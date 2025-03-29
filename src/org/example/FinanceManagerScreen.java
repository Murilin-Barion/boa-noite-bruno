package org.example;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FinanceManagerScreen extends JFrame {
    private JTextField valueField, dateField, descriptionField;
    private JComboBox<String> categoryComboBox, typeComboBox;
    private JLabel balanceLabel, incomeLabel, expenseLabel;

    public FinanceManagerScreen(Usuario user) {
        setTitle("Gerenciador Financeiro");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Barra de menu do usuário
        JMenuBar menuBar = new JMenuBar();
        JMenu userMenu = new JMenu("Usuário: " + user.getNome());
        JMenuItem logoutItem = new JMenuItem("Sair");
        logoutItem.addActionListener(e -> {
            this.dispose();
            new LoginScreen().setVisible(true);
        });
        userMenu.add(logoutItem);
        menuBar.add(userMenu);
        setJMenuBar(menuBar);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --------------------------- FORMULÁRIO DE CADASTRO DE TRANSACOES ---------------------------
        // Painel para o formulário de transações
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Cadastro de Transações"));

        formPanel.add(new JLabel("Tipo:"));
        typeComboBox = new JComboBox<>(new String[]{"Receita", "Despesa"});
        formPanel.add(typeComboBox);

        formPanel.add(new JLabel("Categoria:"));
        categoryComboBox = new JComboBox<>();
        formPanel.add(categoryComboBox);

        formPanel.add(new JLabel("Data:"));
        try {
            MaskFormatter dateFormatter = new MaskFormatter("##/##/####");
            dateFormatter.setPlaceholderCharacter('_');
            dateField = new JFormattedTextField(dateFormatter);
            dateField.setColumns(10);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        formPanel.add(dateField);

        formPanel.add(new JLabel("Descrição:"));
        descriptionField = new JTextField();
        formPanel.add(descriptionField);

        formPanel.add(new JLabel("Valor:"));
        valueField = new JTextField();
        formPanel.add(valueField);

        JButton addTransactionButton = new JButton("Adicionar Transação");
        addTransactionButton.setPreferredSize(new Dimension(10, 30));
        addTransactionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // (String) faz o cast para o tipo String
                String tipo = (String) typeComboBox.getSelectedItem();
                String categoria = (String) categoryComboBox.getSelectedItem();
                String data = (String) dateField.getText();
                String descricao = (String) descriptionField.getText();
                String valor = (String) valueField.getText();

                if (tipo == null || data == null || descricao == null || valor == null) {
                    JOptionPane.showMessageDialog(null, "Preencha todos os campos!", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }else {
                    // Definindo o formato esperado
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    Transacao transaction = new Transacao(tipo, new Categoria(categoria), LocalDate.parse(data, formatter), descricao, Double.parseDouble(valor));
                    user.adicionarTransacao(transaction);

                    JOptionPane.showMessageDialog(FinanceManagerScreen.this, "Cadastro realizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    new FinanceManagerScreen(user);
                    dispose();
                }

            }
        });
        formPanel.add(addTransactionButton);

        // --------------------------- PAINÉIS DE HISTÓRICO E RESUMO ---------------------------
        // Painel para o histórico de transações
        JPanel historyPanel = new JPanel();
        historyPanel.setLayout(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("Histórico de Transações"));

        List<String> transactionDescriptions = new ArrayList<>();
        for (Transacao transacao : user.getTransacoes()) {
            transactionDescriptions.add(transacao.getDescricao()); // ou qualquer outro campo que seja String
        }

        JList<String> transactionList = new JList<>(transactionDescriptions.toArray(new String[0]));
        JScrollPane scrollPane = new JScrollPane(transactionList);
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        // Painel para o resumo financeiro
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new GridLayout(3, 1, 10, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Resumo Financeiro"));

        balanceLabel = new JLabel(String.format("Saldo Total: R$ %.2f", user.getTotalTransacoes()));
        incomeLabel = new JLabel(String.format("Receitas: R$ %.2f", user.getTotalReceitas()));
        expenseLabel = new JLabel(String.format("Despesas: R$ %.2f", user.getTotalDespesas()));

        summaryPanel.add(balanceLabel);
        summaryPanel.add(incomeLabel);
        summaryPanel.add(expenseLabel);

        // ---------------------------- PAINEL DE CATEGORIAS ---------------------------
        // Painel para gerenciamento de categorias
        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new BorderLayout(10, 10));
        categoryPanel.setBorder(BorderFactory.createTitledBorder("Gerenciamento de Categorias"));

        List<Categoria> categories = user.getCategorias();

        JPanel categoryContainer = new JPanel();
        categoryContainer.setLayout(new BoxLayout(categoryContainer, BoxLayout.Y_AXIS)); // Organiza os campos verticalmente

        if(categories != null && !categories.isEmpty() ) {
            for (Categoria categoria : categories) {
                JTextField newCategoryField = new JTextField(categoria.getDescricao());
                newCategoryField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30)); // Ajusta a largura
                categoryContainer.add(newCategoryField);
            }
        }

        // Adiciona o painel dentro de um JScrollPane
        JScrollPane categoryScrollPane = new JScrollPane(categoryContainer);
        categoryScrollPane.setPreferredSize(new Dimension(150, 180)); // Ajuste conforme necessário
        categoryScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        categoryScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Adiciona o JScrollPane ao painel principal onde estava seu campo de texto original
        categoryPanel.add(categoryScrollPane, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton addCategoryButton = new JButton("Adicionar Categoria");
        JButton editCategoryButton = new JButton("Editar Categoria");
        JButton deleteCategoryButton = new JButton("Excluir Categoria");

        Dimension buttonSize = new Dimension(150, 30);
        addCategoryButton.setPreferredSize(buttonSize);
        editCategoryButton.setPreferredSize(buttonSize);
        deleteCategoryButton.setPreferredSize(buttonSize);

        buttonPanel.add(addCategoryButton);
        buttonPanel.add(editCategoryButton);
        buttonPanel.add(deleteCategoryButton);

        categoryPanel.add(buttonPanel, BorderLayout.CENTER);

        // Adicionando os painéis ao painel principal
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout(10, 10));
        leftPanel.add(formPanel, BorderLayout.NORTH);
        leftPanel.add(categoryPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout(10, 10));
        rightPanel.add(historyPanel, BorderLayout.CENTER);
        rightPanel.add(summaryPanel, BorderLayout.SOUTH);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        add(mainPanel);
        setVisible(true);
    }
}