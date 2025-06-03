package org.example;

import org.example.dao.CategoriaDAO;
import org.example.dao.TransacaoDAO;
import org.example.dao.UsuarioDAO;
import org.example.dao.impl.CategoriaDAOImpl;
import org.example.dao.impl.TransacaoDAOImpl;
import org.example.dao.impl.UsuarioDAOImpl;
import org.hibernate.Session;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
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
    private JList<String> transactionList;
    private JFormattedTextField startDateField, endDateField;
    private JComboBox<String> filterTypeCombo;
    private Usuario currentUser;


    //Importe informar que coleções de LAZY é algo que ta me tirando as paciencias. Java nunca mais
    public FinanceManagerScreen(Usuario user) throws ParseException {
        try (Session session = HibernateUtil.openSession()) {
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl(session);
            this.currentUser = usuarioDAO.findById(user.getId());
            org.hibernate.Hibernate.initialize(currentUser.getTransacoes());
            org.hibernate.Hibernate.initialize(currentUser.getCategorias());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados do usuário: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            new LoginScreen().setVisible(true); // Retorna para a tela de login em caso de erro
            dispose();
            return;
        }

        setTitle("Gerenciador Financeiro");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

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

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --------------------------- FORMULÁRIO DE CADASTRO DE TRANSACOES ---------------------------
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Cadastro de Transações"));

        formPanel.add(new JLabel("Tipo:"));
        typeComboBox = new JComboBox<>(new String[]{"Receita", "Despesa"});
        formPanel.add(typeComboBox);

        formPanel.add(new JLabel("Categoria:"));
        categoryComboBox = new JComboBox<>();
        updateCategoryComboBoxes(currentUser);
        formPanel.add(categoryComboBox);

        formPanel.add(new JLabel("Data:"));
        MaskFormatter dateFormatter = new MaskFormatter("##/##/####");
        dateFormatter.setPlaceholderCharacter('_');
        dateField = new JFormattedTextField(dateFormatter);
        dateField.setColumns(10);
        formPanel.add(dateField);

        formPanel.add(new JLabel("Descrição:"));
        descriptionField = new JTextField();
        formPanel.add(descriptionField);

        formPanel.add(new JLabel("Valor:"));
        valueField = new JTextField();
        formPanel.add(valueField);

        JButton addTransactionButton = new JButton("Adicionar Transação");
        addTransactionButton.setPreferredSize(new Dimension(10, 30));
        addTransactionButton.addActionListener(e -> {
            String tipo = (String) typeComboBox.getSelectedItem();
            String categoriaDesc = (String) categoryComboBox.getSelectedItem();
            String dataStr = (String) dateField.getText();
            String descricao = (String) descriptionField.getText();
            String valorStr = (String) valueField.getText();

            if (tipo == null || categoriaDesc == null || dataStr == null || descricao == null || valorStr == null || dataStr.contains("_") || dataStr.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Preencha todos os campos corretamente!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Session session = HibernateUtil.openSession()) {
                CategoriaDAO categoriaDAO = new CategoriaDAOImpl(session);
                TransacaoDAO transacaoDAO = new TransacaoDAOImpl(session);
                UsuarioDAO usuarioDAO = new UsuarioDAOImpl(session);

                Categoria categoria = categoriaDAO.findByUsuarioAndDescricao(currentUser, categoriaDesc);
                if (categoria == null) {
                    JOptionPane.showMessageDialog(FinanceManagerScreen.this, "Categoria selecionada não encontrada.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate data = LocalDate.parse(dataStr, formatter);
                double valor = Double.parseDouble(valorStr);

                Transacao transaction = new Transacao(tipo, categoria, data, descricao, valor, currentUser);

                currentUser.adicionarTransacao(transaction);
                transacaoDAO.save(transaction);

                JOptionPane.showMessageDialog(FinanceManagerScreen.this, "Transação adicionada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                currentUser = usuarioDAO.findById(currentUser.getId());
                org.hibernate.Hibernate.initialize(currentUser.getTransacoes());
                org.hibernate.Hibernate.initialize(currentUser.getCategorias());

                updateFinanceData(); // Atualiza dados financeiros e lista de transações
                clearTransactionFormFields();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(FinanceManagerScreen.this, "Valor inválido. Por favor, digite um número.", "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(FinanceManagerScreen.this, "Erro ao adicionar transação: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        formPanel.add(addTransactionButton);

        // --------------------------- PAINÉIS DE HISTÓRICO E RESUMO ---------------------------
        JPanel historyPanel = new JPanel();
        historyPanel.setLayout(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("Histórico de Transações"));

        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtrar Transações"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; filterPanel.add(new JLabel("Data Inicial:"), gbc);
        gbc.gridx = 1; startDateField = new JFormattedTextField(dateFormatter); startDateField.setColumns(10); filterPanel.add(startDateField, gbc);
        gbc.gridx = 2; filterPanel.add(new JLabel("Data Final:"), gbc);
        gbc.gridx = 3; endDateField = new JFormattedTextField(dateFormatter); endDateField.setColumns(10); filterPanel.add(endDateField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; filterPanel.add(new JLabel("Tipo:"), gbc);
        gbc.gridx = 1; filterTypeCombo = new JComboBox<>(new String[]{"Todos", "Receita", "Despesa"}); filterPanel.add(filterTypeCombo, gbc);

        gbc.gridx = 2; filterPanel.add(new JLabel("Categoria:"), gbc);
        gbc.gridx = 3; managerCategoryCombo = new JComboBox<>(); updateManagerCategoryComboBox(currentUser); filterPanel.add(managerCategoryCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.CENTER;
        JButton filterButton = new JButton("Aplicar Filtros");
        filterPanel.add(filterButton, gbc);
        historyPanel.add(filterPanel, BorderLayout.NORTH);

        transactionList = new JList<>();
        JScrollPane scrollPane = new JScrollPane(transactionList);
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new GridLayout(3, 1, 10, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Resumo Financeiro"));

        balanceLabel = new JLabel();
        incomeLabel = new JLabel();
        expenseLabel = new JLabel();
        summaryPanel.add(balanceLabel);
        summaryPanel.add(incomeLabel);
        summaryPanel.add(expenseLabel);

        // ---------------------------- PAINEL DE CATEGORIAS ---------------------------
        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new BorderLayout(10, 10));
        categoryPanel.setBorder(BorderFactory.createTitledBorder("Gerenciamento de Categorias"));

        categoryListModel = new DefaultListModel<>();
        categoryList = new JList<>(categoryListModel);
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane categoryScrollPane = new JScrollPane(categoryList);
        categoryPanel.add(categoryScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton addCategoryButton = new JButton("Adicionar Categoria");
        JButton editCategoryButton = new JButton("Editar Categoria");
        JButton deleteCategoryButton = new JButton("Excluir Categoria");

        // LISTENERS DE CATEGORIA
        addCategoryButton.addActionListener(e -> {
            String novaCategoriaDesc = JOptionPane.showInputDialog(this, "Digite o nome da nova categoria:");
            if (novaCategoriaDesc != null && !novaCategoriaDesc.trim().isEmpty()) {

                addCategoryButton.setEnabled(false);

                SwingWorker<Categoria, Void> worker = new SwingWorker<Categoria, Void>() {
                    private Exception error = null;
                    private boolean alreadyExists = false;
                    private Categoria savedCategoria = null;

                    @Override
                    protected Categoria doInBackground() throws Exception {
                        try (Session session = HibernateUtil.openSession()) {
                            CategoriaDAO categoriaDAO = new CategoriaDAOImpl(session);

                            if (categoriaDAO.findByUsuarioAndDescricao(currentUser, novaCategoriaDesc.trim()) != null) {
                                alreadyExists = true;
                                return null;
                            }

                            Categoria novaCategoria = new Categoria(novaCategoriaDesc.trim(), currentUser);

                            categoriaDAO.save(novaCategoria);

                            savedCategoria = novaCategoria;
                            return savedCategoria;

                        } catch (Exception ex) {
                            this.error = ex;
                            return null;
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            if (error != null) {
                                JOptionPane.showMessageDialog(FinanceManagerScreen.this, "Erro ao adicionar categoria: " + error.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                                error.printStackTrace();
                            } else if (alreadyExists) {
                                JOptionPane.showMessageDialog(FinanceManagerScreen.this, "Erro: Categoria com este nome já existe!", "Erro", JOptionPane.ERROR_MESSAGE);
                            } else {
                                Categoria resultCategoria = get();

                                if (resultCategoria != null) {
                                    if (currentUser.getCategorias() == null) {
                                        currentUser.setCategorias(new ArrayList<>()); // Initialize if null
                                    }
                                    boolean found = false;
                                    for(Categoria cat : currentUser.getCategorias()) {
                                        if(cat.getId() != null && cat.getId().equals(resultCategoria.getId())) {
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found) {
                                        currentUser.getCategorias().add(resultCategoria);
                                    }

                                    updateCategoryList(currentUser);
                                    updateCategoryComboBoxes(currentUser);
                                    updateManagerCategoryComboBox(currentUser);


                                    JOptionPane.showMessageDialog(FinanceManagerScreen.this, "Categoria adicionada com sucesso!");
                                } else if (!alreadyExists) {
                                    JOptionPane.showMessageDialog(FinanceManagerScreen.this, "Erro: Não foi possível adicionar a categoria.", "Erro", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(FinanceManagerScreen.this, "Erro ao finalizar adição de categoria: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                            ex.printStackTrace();
                        } finally {
                            addCategoryButton.setEnabled(true);
                        }
                    }
                };
                worker.execute();
            }
        });

        editCategoryButton.addActionListener(e -> {
            int selectedIndex = categoryList.getSelectedIndex();
            if (selectedIndex == -1) {
                JOptionPane.showMessageDialog(this, "Selecione uma categoria para editar!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<Categoria> categoriasDoUsuario = currentUser.getCategorias();
            if (selectedIndex < 0 || selectedIndex >= categoriasDoUsuario.size()) {
                JOptionPane.showMessageDialog(this, "Seleção de categoria inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            final Long categoriaIdParaEditar = categoriasDoUsuario.get(selectedIndex).getId();
            final String nomeAtualCategoria = categoriasDoUsuario.get(selectedIndex).getDescricao();

            String novoNome = JOptionPane.showInputDialog(this, "Digite o novo nome para a categoria:",
                    nomeAtualCategoria);

            if (novoNome != null && !novoNome.trim().isEmpty()) {
                try (Session session = HibernateUtil.openSession()) {
                    CategoriaDAO categoriaDAO = new CategoriaDAOImpl(session);
                    UsuarioDAO usuarioDAO = new UsuarioDAOImpl(session);

                    Categoria categoriaExistenteComNovoNome = categoriaDAO.findByUsuarioAndDescricao(currentUser, novoNome.trim());
                    if (categoriaExistenteComNovoNome != null && !categoriaExistenteComNovoNome.getId().equals(categoriaIdParaEditar)) {
                        JOptionPane.showMessageDialog(this, "Erro: Categoria com este nome já existe para você!", "Erro", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    Categoria categoriaEmSessao = categoriaDAO.findById(categoriaIdParaEditar);
                    if (categoriaEmSessao != null) {
                        categoriaEmSessao.setDescricao(novoNome.trim());
                        categoriaDAO.save(categoriaEmSessao);

                        JOptionPane.showMessageDialog(this, "Categoria editada com sucesso!");
                        currentUser = usuarioDAO.findById(currentUser.getId());
                        org.hibernate.Hibernate.initialize(currentUser.getCategorias());
                        updateCategoryList(currentUser);
                        updateCategoryComboBoxes(currentUser);
                        updateManagerCategoryComboBox(currentUser);
                        updateFinanceData();
                    } else {
                        JOptionPane.showMessageDialog(this, "Erro: Categoria não encontrada no banco de dados.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao editar categoria: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        deleteCategoryButton.addActionListener(e -> {
            int selectedIndex = categoryList.getSelectedIndex();
            if (selectedIndex == -1) {
                JOptionPane.showMessageDialog(this, "Selecione uma categoria para excluir!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<Categoria> categoriasDoUsuario = currentUser.getCategorias();
            if (selectedIndex < 0 || selectedIndex >= categoriasDoUsuario.size()) {
                JOptionPane.showMessageDialog(this, "Seleção de categoria inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            final Categoria categoriaParaDeletar = categoriasDoUsuario.get(selectedIndex);


            int confirm = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja excluir a categoria '" + categoriaParaDeletar.getDescricao() + "'?",
                    "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try (Session session = HibernateUtil.openSession()) {
                    CategoriaDAO categoriaDAO = new CategoriaDAOImpl(session);
                    TransacaoDAO transacaoDAO = new TransacaoDAOImpl(session);
                    UsuarioDAO usuarioDAO = new UsuarioDAOImpl(session);

                    List<Transacao> transacoesComCategoria = transacaoDAO.findByUsuarioAndFilters(currentUser, null, null, "Todos", categoriaParaDeletar.getDescricao());
                    if (!transacoesComCategoria.isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                                "Erro: Não é possível excluir esta categoria pois ela está em uso por alguma transação!",
                                "Erro", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (currentUser.removerCategoria(selectedIndex)) {
                        categoriaDAO.delete(categoriaParaDeletar.getId());

                        JOptionPane.showMessageDialog(this, "Categoria excluída com sucesso!");

                        currentUser = usuarioDAO.findById(currentUser.getId());
                        org.hibernate.Hibernate.initialize(currentUser.getCategorias());
                        updateCategoryList(currentUser);
                        updateCategoryComboBoxes(currentUser);
                        updateManagerCategoryComboBox(currentUser);
                    } else {
                        JOptionPane.showMessageDialog(this, "Erro ao remover categoria.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao excluir categoria: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        buttonPanel.add(addCategoryButton);
        buttonPanel.add(editCategoryButton);
        buttonPanel.add(deleteCategoryButton);
        categoryPanel.add(buttonPanel, BorderLayout.SOUTH);

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

        filterButton.addActionListener(e -> {
            updateFinanceData();
        });

        updateFinanceData();
        updateCategoryList(currentUser);
    }

    private void clearTransactionFormFields() {
        valueField.setText("");
        dateField.setText("");
        descriptionField.setText("");
        typeComboBox.setSelectedIndex(0);
        if (categoryComboBox.getItemCount() > 0) {
            categoryComboBox.setSelectedIndex(0);
        }
    }

    private void updateCategoryList(Usuario user) {
        categoryListModel.clear();
        if (user.getCategorias() != null) {
            for (Categoria categoria : user.getCategorias()) {
                categoryListModel.addElement(categoria.getDescricao());
            }
        }
    }

    private void updateCategoryComboBoxes(Usuario user) {
        categoryComboBox.removeAllItems();
        if (user.getCategorias() != null) {
            for (Categoria categoria : user.getCategorias()) {
                categoryComboBox.addItem(categoria.getDescricao());
            }
        }
    }

    private void updateManagerCategoryComboBox(Usuario user) {
        managerCategoryCombo.removeAllItems();
        managerCategoryCombo.addItem("Todas");
        if (user.getCategorias() != null) {
            for (Categoria categoria : user.getCategorias()) {
                managerCategoryCombo.addItem(categoria.getDescricao());
            }
        }
    }


    private void updateFinanceData() {
        try (Session session = HibernateUtil.openSession()) {
            TransacaoDAO transacaoDAO = new TransacaoDAOImpl(session);
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl(session);

            currentUser = usuarioDAO.findById(currentUser.getId());
            org.hibernate.Hibernate.initialize(currentUser.getTransacoes());

            balanceLabel.setText(String.format("Saldo Total: R$ %.2f", currentUser.getTotalTransacoes()));
            incomeLabel.setText(String.format("Receitas: R$ %.2f", currentUser.getTotalReceitas()));
            expenseLabel.setText(String.format("Despesas: R$ %.2f", currentUser.getTotalDespesas()));

            String startDateStr = startDateField.getText();
            String endDateStr = endDateField.getText();
            String filterType = (String) filterTypeCombo.getSelectedItem();
            String filterCategory = (String) managerCategoryCombo.getSelectedItem();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate startDate = null;
            LocalDate endDate = null;

            if (startDateStr != null && !startDateStr.replaceAll("[_/ ]", "").isEmpty()) {
                try {
                    startDate = LocalDate.parse(startDateStr, formatter);
                } catch (java.time.format.DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Data Inicial inválida. Formato esperado DD/MM/AAAA.", "Erro de Filtro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (endDateStr != null && !endDateStr.replaceAll("[_/ ]", "").isEmpty()) {
                try {
                    endDate = LocalDate.parse(endDateStr, formatter);
                } catch (java.time.format.DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Data Final inválida. Formato esperado DD/MM/AAAA.", "Erro de Filtro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                JOptionPane.showMessageDialog(this, "Data inicial não pode ser depois da data final.", "Erro de Filtro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<Transacao> filteredTransactions = transacaoDAO.findByUsuarioAndFilters(currentUser, startDate, endDate, filterType, filterCategory);

            List<String> transactionDescriptions = new ArrayList<>();
            for (Transacao transacao : filteredTransactions) {
                String entry = String.format("%-10s | %-7s | %-20s | R$ %8.2f | %s",
                        transacao.getData().format(formatter),
                        transacao.getTipo(),
                        transacao.getDescricao().length() > 20 ?
                                transacao.getDescricao().substring(0, 17) + "..." :
                                transacao.getDescricao(),
                        transacao.getValor(),
                        transacao.getCategoria().getDescricao());
                transactionDescriptions.add(entry);
            }
            transactionList.setListData(transactionDescriptions.toArray(new String[0]));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar dados financeiros: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}