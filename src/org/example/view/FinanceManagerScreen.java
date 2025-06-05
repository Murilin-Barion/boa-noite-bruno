package org.example.view;

import org.example.controller.FinanceController;
import org.example.model.Categoria;
import org.example.model.Transacao;
import org.example.model.Usuario;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

// Tela Financeira com layout EXATAMENTE igual ao original, usando Controller
public class FinanceManagerScreen extends JFrame {
    // --- Componentes da UI (Declarações do original) ---
    private JTextField valueField, descriptionField;
    // Corrigido: dateField era JTextField no original, mas precisa ser JFormattedTextField pela máscara
    private JFormattedTextField dateField, startDateField, endDateField;
    private JComboBox<String> categoryComboBox, typeComboBox, managerCategoryCombo, filterTypeCombo;
    private JLabel balanceLabel, incomeLabel, expenseLabel;
    private DefaultListModel<String> categoryListModel;
    private JList<String> categoryList;
    // Corrigido: transactionList era JList<String> no original, mas precisa de Model
    private DefaultListModel<String> transactionListModel;
    private JList<String> transactionList;

    // --- Atributos MVC ---
    private Usuario currentUser; // Mantido para exibir nome, etc.
    private FinanceController financeController; // Controller para lógica
    private final DateTimeFormatter viewDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FinanceManagerScreen(Usuario user) throws ParseException {
        this.currentUser = user; // Armazena o usuário passado pelo Login
        this.financeController = new FinanceController(user);
        this.currentUser = financeController.getCurrentUser(); // Pega dados atualizados

        if (this.currentUser == null) {
            JOptionPane.showMessageDialog(this, "Erro fatal ao carregar dados do usuário. Saindo.", "Erro", JOptionPane.ERROR_MESSAGE);
            new LoginScreen().setVisible(true);
            dispose();
            return;
        }

        // --- Configurações básicas da janela (do original) ---
        setTitle("Gerenciador Financeiro");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Construção da UI (Layout EXATO do original, lógica adaptada) ---
        setupUI();

        // --- Carregamento inicial dos dados via Controller ---
        updateFinanceData();
        updateCategoryList();
        updateCategoryComboBoxes();
        updateManagerCategoryComboBox();
    }

    // Método para criar o MaskFormatter (do original)
    private MaskFormatter createDateFormatter() {
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter("##/##/####");
            formatter.setPlaceholderCharacter('_');
        } catch (ParseException e) {
            System.err.println("Erro inesperado ao criar MaskFormatter: " + e.getMessage());
        }
        return formatter;
    }

    // Configura a UI principal (estrutura EXATA do original)
    private void setupUI() {
        // --- Menu Bar (original) ---
        JMenuBar menuBar = new JMenuBar();
        JMenu userMenu = new JMenu("Usuário: " + currentUser.getNome());
        JMenuItem logoutItem = new JMenuItem("Sair");
        logoutItem.addActionListener(e -> {
            this.dispose();
            new LoginScreen().setVisible(true);
        });
        userMenu.add(logoutItem);
        menuBar.add(userMenu);
        setJMenuBar(menuBar);

        // --- Painel Principal (original) ---
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- FORMULÁRIO DE CADASTRO DE TRANSACOES (Layout original) ---
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(6, 2, 10, 10)); // 6 linhas, 2 colunas
        formPanel.setBorder(BorderFactory.createTitledBorder("Cadastro de Transações"));

        formPanel.add(new JLabel("Tipo:"));
        typeComboBox = new JComboBox<>(new String[]{"Receita", "Despesa"});
        formPanel.add(typeComboBox);

        formPanel.add(new JLabel("Categoria:"));
        categoryComboBox = new JComboBox<>(); // Populado depois
        formPanel.add(categoryComboBox);

        formPanel.add(new JLabel("Data:")); // Label original sem formato
        MaskFormatter originalDateFormatter = createDateFormatter(); // Usa a máscara
        dateField = new JFormattedTextField(originalDateFormatter);
        dateField.setColumns(10);
        formPanel.add(dateField);

        formPanel.add(new JLabel("Descrição:"));
        descriptionField = new JTextField();
        formPanel.add(descriptionField);

        formPanel.add(new JLabel("Valor:"));
        valueField = new JTextField();
        formPanel.add(valueField);

        // Botão Adicionar Transação (layout original)
        JButton addTransactionButton = new JButton("Adicionar Transação");
        addTransactionButton.setPreferredSize(new Dimension(10, 30)); // Tamanho original
        // --- ActionListener adaptado para usar Controller ---
        addTransactionButton.addActionListener(e -> addTransactionAction());
        // Adiciona o botão na última linha, primeira coluna (como no original)
        formPanel.add(addTransactionButton);
        formPanel.add(new JLabel()); // Placeholder na última linha, segunda coluna

        // --- PAINÉIS DE HISTÓRICO E RESUMO (Layout original) ---
        // Painel intermediário historyAndSummaryPanel removido para seguir estrutura original

        // --- Painel Histórico (original) ---
        JPanel historyPanel = new JPanel();
        historyPanel.setLayout(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("Histórico de Transações"));

        // --- Painel Filtro (layout original com GridBagLayout) ---
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtrar Transações"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        MaskFormatter filterDateFormatter = createDateFormatter();

        gbc.gridx = 0; gbc.gridy = 0; filterPanel.add(new JLabel("Data Inicial:"), gbc);
        gbc.gridx = 1; startDateField = new JFormattedTextField(filterDateFormatter); startDateField.setColumns(10); filterPanel.add(startDateField, gbc);
        gbc.gridx = 2; filterPanel.add(new JLabel("Data Final:"), gbc);
        gbc.gridx = 3; endDateField = new JFormattedTextField(filterDateFormatter); endDateField.setColumns(10); filterPanel.add(endDateField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; filterPanel.add(new JLabel("Tipo:"), gbc);
        gbc.gridx = 1; filterTypeCombo = new JComboBox<>(new String[]{"Todos", "Receita", "Despesa"}); filterPanel.add(filterTypeCombo, gbc);

        gbc.gridx = 2; filterPanel.add(new JLabel("Categoria:"), gbc);
        gbc.gridx = 3; managerCategoryCombo = new JComboBox<>(); // Populado depois
        filterPanel.add(managerCategoryCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.CENTER; // Configuração exata do original
        JButton filterButton = new JButton("Aplicar Filtros");
        // --- ActionListener adaptado para usar Controller ---
        filterButton.addActionListener(e -> filterTransactionsAction());
        filterPanel.add(filterButton, gbc);

        historyPanel.add(filterPanel, BorderLayout.NORTH);

        // --- Lista de Transações (original) ---
        transactionListModel = new DefaultListModel<>();
        transactionList = new JList<>(transactionListModel);
        JScrollPane scrollPane = new JScrollPane(transactionList);
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Painel Resumo (original) ---
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new GridLayout(3, 1, 10, 10)); // 3 linhas, 1 coluna
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Resumo Financeiro"));

        balanceLabel = new JLabel(); // Texto removido para igualar ao original
        incomeLabel = new JLabel();
        expenseLabel = new JLabel();
        summaryPanel.add(balanceLabel);
        summaryPanel.add(incomeLabel);
        summaryPanel.add(expenseLabel);

        // Adições ao painel intermediário removidas

        // --- PAINEL DE CATEGORIAS (Layout original) ---
        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new BorderLayout(10, 10));
        categoryPanel.setBorder(BorderFactory.createTitledBorder("Gerenciamento de Categorias"));
        // Tamanho preferencial removido para igualar ao original

        categoryListModel = new DefaultListModel<>();
        categoryList = new JList<>(categoryListModel);
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane categoryScrollPane = new JScrollPane(categoryList);
        categoryPanel.add(categoryScrollPane, BorderLayout.CENTER);

        // --- Painel de Botões de Categoria (FlowLayout original) ---
        JPanel categoryButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton addCategoryButton = new JButton("Adicionar Categoria");
        JButton editCategoryButton = new JButton("Editar Categoria");
        JButton deleteCategoryButton = new JButton("Excluir Categoria");

        // --- ActionListeners adaptados para usar Controller ---
        addCategoryButton.addActionListener(e -> addCategoryAction());
        editCategoryButton.addActionListener(e -> editCategoryAction());
        deleteCategoryButton.addActionListener(e -> deleteCategoryAction());

        categoryButtonPanel.add(addCategoryButton);
        categoryButtonPanel.add(editCategoryButton);
        categoryButtonPanel.add(deleteCategoryButton);

        categoryPanel.add(categoryButtonPanel, BorderLayout.SOUTH);

        // --- Cria painéis de agrupamento para layout correto (baseado na imagem) ---
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10)); // Painel para agrupar form e categorias verticalmente
        leftPanel.add(formPanel, BorderLayout.NORTH);
        leftPanel.add(categoryPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(0, 10)); // Painel para agrupar histórico e resumo verticalmente
        rightPanel.add(historyPanel, BorderLayout.CENTER);
        rightPanel.add(summaryPanel, BorderLayout.SOUTH);

        // --- Altera layout do mainPanel e adiciona painéis agrupados ---
        mainPanel.setLayout(new GridLayout(1, 2, 10, 10)); // Layout 1 linha, 2 colunas com espaçamento
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        add(mainPanel);
    }

    // --- Métodos de Ação (adaptados para usar Controller) ---

    private void addTransactionAction() {
        String tipo = (String) typeComboBox.getSelectedItem();
        String categoriaDesc = (String) categoryComboBox.getSelectedItem();
        String dataStrRaw = dateField.getText().replace("/", "").trim();
        String dataStrFormatted = dateField.getText();
        String descricao = descriptionField.getText().trim();
        String valorStr = valueField.getText().trim();

        if (tipo == null || categoriaDesc == null || dataStrRaw.contains("_") || dataStrRaw.length() != 8 || descricao.isEmpty() || valorStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos corretamente (Data: dd/MM/yyyy)!", "Erro de Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate data = LocalDate.parse(dataStrFormatted, viewDateFormatter);
            double valor = Double.parseDouble(valorStr.replace(",", "."));

            financeController.addTransaction(tipo, categoriaDesc, data, descricao, valor);

            JOptionPane.showMessageDialog(this, "Transação adicionada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            updateFinanceData();
            clearTransactionFormFields();
            revalidate(); // Adicionado conforme solicitado
            repaint();    // Adicionado conforme solicitado

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de data inválido. Use dd/MM/yyyy.", "Erro de Data", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor inválido. Use ponto como separador decimal.", "Erro de Valor", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar transação: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void filterTransactionsAction() {
        LocalDate startDate = null;
        LocalDate endDate = null;
        String tipo = (String) filterTypeCombo.getSelectedItem();
        String categoriaDesc = (String) managerCategoryCombo.getSelectedItem();

        try {
            String startDateStrRaw = startDateField.getText().replace("/", "").trim();
            if (!startDateStrRaw.contains("_") && startDateStrRaw.length() == 8) {
                startDate = LocalDate.parse(startDateField.getText(), viewDateFormatter);
            }
            String endDateStrRaw = endDateField.getText().replace("/", "").trim();
            if (!endDateStrRaw.contains("_") && endDateStrRaw.length() == 8) {
                endDate = LocalDate.parse(endDateField.getText(), viewDateFormatter);
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de data inválido nos filtros. Use dd/MM/yyyy.", "Erro de Data", JOptionPane.ERROR_MESSAGE);
            return;
        }

        tipo = "Todos".equalsIgnoreCase(tipo) ? null : tipo;
        categoriaDesc = "Todos".equalsIgnoreCase(categoriaDesc) ? null : categoriaDesc;

        List<Transacao> filteredTransactions = financeController.filterTransactions(startDate, endDate, tipo, categoriaDesc);
        updateTransactionList(filteredTransactions);
        updateSummary(filteredTransactions);
    }

    private void addCategoryAction() {
        String novaCategoriaDesc = JOptionPane.showInputDialog(this, "Digite o nome da nova categoria:");
        if (novaCategoriaDesc != null && !novaCategoriaDesc.trim().isEmpty()) {
            try {
                financeController.addCategory(novaCategoriaDesc.trim());
                JOptionPane.showMessageDialog(this, "Categoria adicionada com sucesso!");
                updateCategoryList();
                updateCategoryComboBoxes();
                updateManagerCategoryComboBox();
                revalidate(); // Adicionado conforme solicitado
                repaint();    // Adicionado conforme solicitado
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao adicionar categoria: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void editCategoryAction() {
        int selectedIndex = categoryList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma categoria para editar!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Categoria> categorias = financeController.getCategoriasDoUsuario();
        if (selectedIndex < 0 || selectedIndex >= categorias.size()) {
            JOptionPane.showMessageDialog(this, "Erro: Seleção inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Categoria categoriaSelecionada = categorias.get(selectedIndex);
        Long categoriaId = categoriaSelecionada.getId();
        String nomeAtual = categoriaSelecionada.getDescricao();

        String novoNome = JOptionPane.showInputDialog(this, "Digite o novo nome para a categoria:", nomeAtual);

        if (novoNome != null && !novoNome.trim().isEmpty() && !novoNome.trim().equals(nomeAtual)) {
            try {
                financeController.editCategory(categoriaId, novoNome.trim());
                JOptionPane.showMessageDialog(this, "Categoria editada com sucesso!");
                updateCategoryList();
                updateCategoryComboBoxes();
                updateManagerCategoryComboBox();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao editar categoria: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void deleteCategoryAction() {
        int selectedIndex = categoryList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma categoria para excluir!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Categoria> categorias = financeController.getCategoriasDoUsuario();
        if (selectedIndex < 0 || selectedIndex >= categorias.size()) {
            JOptionPane.showMessageDialog(this, "Erro: Seleção inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Categoria categoriaSelecionada = categorias.get(selectedIndex);
        Long categoriaId = categoriaSelecionada.getId();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir a categoria '" + categoriaSelecionada.getDescricao() + "'?",
                "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                financeController.deleteCategory(categoriaId);
                JOptionPane.showMessageDialog(this, "Categoria excluída com sucesso!");
                updateCategoryList();
                updateCategoryComboBoxes();
                updateManagerCategoryComboBox();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir categoria: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    // --- Métodos de Atualização da UI (usam Controller) ---

    private void updateFinanceData() {
        List<Transacao> transactions = financeController.getTransacoesDoUsuario();
        updateTransactionList(transactions);
        updateSummary(transactions);
    }

    private void updateTransactionList(List<Transacao> transactions) {
        transactionListModel.clear();
        if (transactions != null) {
            // Formato original da lista de transações
            DateTimeFormatter listDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (Transacao t : transactions) {
                transactionListModel.addElement(
                        String.format("%s | %s | %s | R$ %.2f | %s",
                                t.getData().format(listDateFormatter),
                                t.getTipo(),
                                t.getDescricao(),
                                t.getValor(),
                                t.getCategoria().getDescricao()
                        )
                );
            }
        }
    }

    private void updateSummary(List<Transacao> transactions) {
        FinanceController.FinancialSummary summary = financeController.calculateSummary(transactions);
        balanceLabel.setText(String.format("Saldo Total: R$ %.2f", summary.balance)); // Texto original
        incomeLabel.setText(String.format("Receitas: R$ %.2f", summary.totalIncome));
        expenseLabel.setText(String.format("Despesas: R$ %.2f", summary.totalExpense));
    }

    private void updateCategoryList() {
        categoryListModel.clear();
        List<Categoria> categorias = financeController.getCategoriasDoUsuario();
        if (categorias != null) {
            for (Categoria c : categorias) {
                categoryListModel.addElement(c.getDescricao());
            }
        }
    }

    private void updateCategoryComboBoxes() {
        String selectedCategory = (String) categoryComboBox.getSelectedItem(); // Salva seleção atual
        categoryComboBox.removeAllItems();
        List<Categoria> categorias = financeController.getCategoriasDoUsuario();
        if (categorias != null) {
            for (Categoria c : categorias) {
                categoryComboBox.addItem(c.getDescricao());
            }
        }
        categoryComboBox.setSelectedItem(selectedCategory); // Tenta restaurar seleção
    }

    private void updateManagerCategoryComboBox() {
        String selectedCategory = (String) managerCategoryCombo.getSelectedItem(); // Salva seleção atual
        managerCategoryCombo.removeAllItems();
        managerCategoryCombo.addItem("Todas");
        List<Categoria> categorias = financeController.getCategoriasDoUsuario();
        if (categorias != null) {
            for (Categoria c : categorias) {
                managerCategoryCombo.addItem(c.getDescricao());
            }
        }
        managerCategoryCombo.setSelectedItem(selectedCategory); // Tenta restaurar seleção
    }

    private void clearTransactionFormFields() {
        typeComboBox.setSelectedIndex(0);
        categoryComboBox.setSelectedIndex(categoryComboBox.getItemCount() > 0 ? 0 : -1);
        dateField.setValue(null);
        descriptionField.setText("");
        valueField.setText("");
    }
}

