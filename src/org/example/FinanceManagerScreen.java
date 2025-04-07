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

public class FinanceManagerScreen extends JFrame {
    private JTextField valueField, dateField, descriptionField;
    private JComboBox<String> categoryComboBox, typeComboBox, managerCategoryCombo;
    private JLabel balanceLabel, incomeLabel, expenseLabel;
    private DefaultListModel<String> categoryListModel;
    private JList<String> categoryList;

    public FinanceManagerScreen(Usuario user) throws ParseException {
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

                if (tipo == null || categoria == null || data == null || descricao == null || valor == null) {
                    data.contains("_");
                    JOptionPane.showMessageDialog(null, "Preencha todos os campos!", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }else {
                    // Definindo o formato esperado
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    Transacao transaction = new Transacao(tipo, new Categoria(categoria), LocalDate.parse(data, formatter), descricao, Double.parseDouble(valor));
                    user.adicionarTransacao(transaction);

                    JOptionPane.showMessageDialog(FinanceManagerScreen.this, "Cadastro realizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    try {
                        new FinanceManagerScreen(user);
                    } catch (ParseException ex) {
                        throw new RuntimeException(ex);
                    }
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

        //painel de filtros
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtrar Transações"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Filtro por data
        gbc.gridx = 0;
        gbc.gridy = 0;
        filterPanel.add(new JLabel("Data Inicial:"), gbc);

        gbc.gridx = 1;
        JFormattedTextField startDateField;
        startDateField = new JFormattedTextField(new MaskFormatter("##/##/####"));
        startDateField.setColumns(10);
        filterPanel.add(startDateField, gbc);


        gbc.gridx = 2;
        filterPanel.add(new JLabel("Data Final:"), gbc);

        gbc.gridx = 3;
        JFormattedTextField endDateField;
        endDateField = new JFormattedTextField(new MaskFormatter("##/##/####"));
        endDateField.setColumns(10);
        filterPanel.add(endDateField, gbc);

        // Filtro por tipo
        gbc.gridx = 0;
        gbc.gridy = 1;
        filterPanel.add(new JLabel("Tipo:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> filterTypeCombo = new JComboBox<>(new String[]{"Todos", "Receita", "Despesa"});
        filterPanel.add(filterTypeCombo, gbc);

        // Filtro por categoria
        gbc.gridx = 2;
        filterPanel.add(new JLabel("Categoria:"), gbc);

        gbc.gridx = 3;
        managerCategoryCombo = new JComboBox<>();
        managerCategoryCombo.addItem("Todas");

        // Verifica se a lista de categorias não é nula antes de iterar
        List<Categoria> categoriasUsuario = user.getCategorias();
        if(categoriasUsuario != null) {
            for (Categoria categoria : categoriasUsuario) {
                managerCategoryCombo.addItem(categoria.getDescricao());
            }
        }

        // E no trecho onde mostra as categorias no painel de gerenciamento
        List<Categoria> listCategories = user.getCategorias();
        JPanel listCategoryContainer = new JPanel();
        listCategoryContainer .setLayout(new BoxLayout(listCategoryContainer, BoxLayout.Y_AXIS));
        if(listCategories != null && !categoriasUsuario.isEmpty()) {
            for (Categoria categoria : listCategories) {
                JTextField newCategoryField = new JTextField(categoria.getDescricao());
                newCategoryField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                listCategoryContainer.add(newCategoryField);
            }
        }
        filterPanel.add(managerCategoryCombo, gbc);

        // Botão de filtrar
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.CENTER;
        JButton filterButton = new JButton("Aplicar Filtros");
        filterPanel.add(filterButton, gbc);

        historyPanel.add(filterPanel, BorderLayout.NORTH);

        List<String> transactionDescriptions = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Transacao transacao : user.getTransacoes()) {
            String entry = String.format("%-10s | %-7s | %-20s | R$ %8.2f | %s",
                    transacao.getData().format(dateFormatter),
                    transacao.getTipo(),
                    transacao.getDescricao().length() > 20 ?
                            transacao.getDescricao().substring(0, 17) + "..." :
                            transacao.getDescricao(),
                    transacao.getValor(),
                    transacao.getCategoria().getDescricao());

            transactionDescriptions.add(entry);
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

        // Lista de categorias
        categoryListModel = new DefaultListModel<>();
        updateCategoryList(user);

        categoryList = new JList<>(categoryListModel);
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane categoryScrollPane = new JScrollPane(categoryList);
        categoryPanel.add(categoryScrollPane, BorderLayout.CENTER);

        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton addCategoryButton = new JButton("Adicionar Categoria");
        JButton editCategoryButton = new JButton("Editar Categoria");
        JButton deleteCategoryButton = new JButton("Excluir Categoria");

        // Listeners dos botões
        addCategoryButton.addActionListener(e -> {
            String novaCategoria = JOptionPane.showInputDialog(this, "Digite o nome da nova categoria:");
            if (novaCategoria != null && !novaCategoria.trim().isEmpty()) {
                if (user.adicionarCategoria(new Categoria(novaCategoria.trim()))) {
                    updateCategoryList(user);
                    updateCategoryComboBoxes(user);
                    updateManagerCategoryComboBox(user);
                    JOptionPane.showMessageDialog(this, "Categoria adicionada com sucesso!");
                } else {
                    JOptionPane.showMessageDialog(this, "Erro: Categoria já existe ou é inválida!", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        editCategoryButton.addActionListener(e -> {
            int selectedIndex = categoryList.getSelectedIndex();
            if (selectedIndex == -1) {
                JOptionPane.showMessageDialog(this, "Selecione uma categoria para editar!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String novoNome = JOptionPane.showInputDialog(this, "Digite o novo nome para a categoria:",
                    user.getCategorias().get(selectedIndex).getDescricao());

            if (novoNome != null && !novoNome.trim().isEmpty()) {
                if (user.editarCategoria(selectedIndex, novoNome.trim())) {
                    updateCategoryList(user);
                    updateCategoryComboBoxes(user);
                    updateManagerCategoryComboBox(user);
                    JOptionPane.showMessageDialog(this, "Categoria editada com sucesso!");
                } else {
                    JOptionPane.showMessageDialog(this, "Erro: Nome inválido ou já existente!", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        deleteCategoryButton.addActionListener(e -> {
            int selectedIndex = categoryList.getSelectedIndex();
            if (selectedIndex == -1) {
                JOptionPane.showMessageDialog(this, "Selecione uma categoria para excluir!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja excluir a categoria '" +
                            user.getCategorias().get(selectedIndex).getDescricao() + "'?",
                    "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (user.removerCategoria(selectedIndex)) {
                    updateCategoryList(user);
                    updateCategoryComboBoxes(user);
                    updateManagerCategoryComboBox(user);
                    JOptionPane.showMessageDialog(this, "Categoria excluída com sucesso!");
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Erro: Não é possível excluir esta categoria pois ela está em uso por alguma transação!",
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        buttonPanel.add(addCategoryButton);
        buttonPanel.add(editCategoryButton);
        buttonPanel.add(deleteCategoryButton);
        categoryPanel.add(buttonPanel, BorderLayout.SOUTH);

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

        // Adicionando o ActionListener ao botão de filtro
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String startDateStr = startDateField.getText();
                String endDateStr = endDateField.getText();
                String filterType = (String) filterTypeCombo.getSelectedItem();
                String filterCategory = (String) managerCategoryCombo.getSelectedItem();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate startDate = null;
                LocalDate endDate = null;

                try {
                    if (!startDateStr.contains("_")) {
                        startDate = LocalDate.parse(startDateStr, formatter);
                    }
                    if (!endDateStr.contains("_")) {
                        endDate = LocalDate.parse(endDateStr, formatter);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(FinanceManagerScreen.this, "Datas inválidas!", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                List<String> filteredDescriptions = new ArrayList<>();
                for (Transacao transacao : user.getTransacoes()) {
                    boolean matches = true;

                    if (startDate != null && transacao.getData().isBefore(startDate)) {
                        matches = false;
                    }
                    if (endDate != null && transacao.getData().isAfter(endDate)) {
                        matches = false;
                    }
                    if (!filterType.equals("Todos") && !transacao.getTipo().equalsIgnoreCase(filterType)) {
                        matches = false;
                    }
                    if (!filterCategory.equals("Todas") && !transacao.getCategoria().getDescricao().equalsIgnoreCase(filterCategory)) {
                        matches = false;
                    }

                    if (matches) {
                        String entry = String.format("%-10s | %-7s | %-20s | R$ %8.2f | %s",
                                transacao.getData().format(formatter),
                                transacao.getTipo(),
                                transacao.getDescricao().length() > 20 ?
                                        transacao.getDescricao().substring(0, 17) + "..." :
                                        transacao.getDescricao(),
                                transacao.getValor(),
                                transacao.getCategoria().getDescricao());

                        filteredDescriptions.add(entry);
                    }
                }

                transactionList.setListData(filteredDescriptions.toArray(new String[0]));
            }
        });
    }

    // Atualiza a lista de categorias exibida na interface gráfica.
    private void updateCategoryList(Usuario user) {
        categoryListModel.clear();
        if (user.getCategorias() != null) {
            for (Categoria categoria : user.getCategorias()) {
                categoryListModel.addElement(categoria.getDescricao());
            }
        }
    }

    private void updateCategoryComboBoxes(Usuario user) {
        // Remove todos os itens atuais do combobox
        categoryComboBox.removeAllItems();
        if (user.getCategorias() != null) {
            for (Categoria categoria : user.getCategorias()) {
                categoryComboBox.addItem(categoria.getDescricao());
            }
        }
    }

    private void updateManagerCategoryComboBox(Usuario user) {
        // Remove todos os itens atuais do combobox
        managerCategoryCombo.removeAllItems();
        managerCategoryCombo.addItem("Todas");
        if (user.getCategorias() != null) {
            for (Categoria categoria : user.getCategorias()) {
                managerCategoryCombo.addItem(categoria.getDescricao());
            }
        }
    }
}