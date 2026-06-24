package views;

import database.UserDAO;
import models.UserModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class UserManagerPanel extends JPanel {

    private MainFrame mainFrame;
    private UserDAO userDAO;

    private JTextField txtUsername, txtFullName;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRole;
    private JTable userTable;
    private DefaultTableModel tableModel;

    public UserManagerPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userDAO = new UserDAO();

        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        initComponents();
        loadUserData();
    }

    private void initComponents() {
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("System Users & Permissions");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(24, 24, 27));
        
        JLabel lblSub = new JLabel("Manage employee accounts and system access roles.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(new Color(113, 113, 122));
        
        headerPanel.add(lblTitle); headerPanel.add(lblSub);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setOpaque(false);
        contentPanel.add(createRegistrationForm(), BorderLayout.WEST);
        contentPanel.add(createTablePanel(), BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createRegistrationForm() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(228, 228, 231), 1, true),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(8, 8, 8, 8);

        JLabel lblHeader = new JLabel("Register New Employee");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHeader.setForeground(new Color(24, 24, 27));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(5, 8, 15, 8); formPanel.add(lblHeader, gbc);

        gbc.gridwidth = 1; gbc.insets = new Insets(6, 8, 6, 8);
        gbc.gridy = 1; gbc.gridx = 0; formPanel.add(createStyledLabel("Full Name *"), gbc);
        txtFullName = new JTextField(15); gbc.gridx = 1; formPanel.add(txtFullName, gbc);

        gbc.gridy = 2; gbc.gridx = 0; formPanel.add(createStyledLabel("Username *"), gbc);
        txtUsername = new JTextField(15); gbc.gridx = 1; formPanel.add(txtUsername, gbc);

        gbc.gridy = 3; gbc.gridx = 0; formPanel.add(createStyledLabel("Password *"), gbc);
        txtPassword = new JPasswordField(15); gbc.gridx = 1; formPanel.add(txtPassword, gbc);

        gbc.gridy = 4; gbc.gridx = 0; formPanel.add(createStyledLabel("Account Role *"), gbc);
        cmbRole = new JComboBox<>(new String[]{"Desk Clerk", "Admin"}); cmbRole.setBackground(Color.WHITE);
        gbc.gridx = 1; formPanel.add(cmbRole, gbc);

        JButton btnSave = new JButton("Create Account");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setBackground(new Color(16, 185, 129)); btnSave.setForeground(Color.WHITE); 
        btnSave.setPreferredSize(new Dimension(0, 40));
        btnSave.addActionListener(e -> saveNewUser());
        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(20, 8, 5, 8); formPanel.add(btnSave, gbc);

        return formPanel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(228, 228, 231), 1, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        String[] cols = {"User ID", "Full Name", "Username", "Role"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        userTable = new JTable(tableModel);
        userTable.setSelectionBackground(new Color(244, 244, 245));
        userTable.setSelectionForeground(new Color(24, 24, 27));
        userTable.setShowGrid(false); 
        userTable.setIntercellSpacing(new Dimension(0, 0));
        
        JTableHeader header = userTable.getTableHeader();
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(228, 228, 231)));
        
        userTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(243, 244, 246)));
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomControls.setBackground(Color.WHITE);
        bottomControls.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JButton btnDelete = new JButton("Remove Selected User");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDelete.setBackground(new Color(239, 68, 68)); 
        btnDelete.setForeground(Color.WHITE); 
        btnDelete.setPreferredSize(new Dimension(200, 40));
        btnDelete.addActionListener(e -> deleteSelectedUser());
        
        bottomControls.add(btnDelete);
        panel.add(bottomControls, BorderLayout.SOUTH);

        return panel;
    }

    private void saveNewUser() {
        if (txtFullName.getText().trim().isEmpty() || txtUsername.getText().trim().isEmpty() || txtPassword.getPassword().length == 0) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.WARNING_MESSAGE); return;
        }
        UserModel newUser = new UserModel(txtUsername.getText().trim(), txtFullName.getText().trim(), cmbRole.getSelectedItem().toString());
        if (userDAO.addUser(newUser, String.valueOf(txtPassword.getPassword()))) {
            JOptionPane.showMessageDialog(this, "User created successfully!");
            txtFullName.setText(""); txtUsername.setText(""); txtPassword.setText("");
            loadUserData();
        } else { JOptionPane.showMessageDialog(this, "Failed to create user. Username may exist.", "Error", JOptionPane.ERROR_MESSAGE); }
    }

    private void deleteSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a user from the table to remove.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        
        int userId = (int) tableModel.getValueAt(row, 0);
        
        if (userId == mainFrame.getCurrentUser().getUserId()) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own account while logged in!", "Security Alert", JOptionPane.ERROR_MESSAGE); return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this user?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (userDAO.deleteUser(userId)) { JOptionPane.showMessageDialog(this, "User removed successfully."); loadUserData(); } 
            else { JOptionPane.showMessageDialog(this, "Failed to remove user.", "Error", JOptionPane.ERROR_MESSAGE); }
        }
    }

    private void loadUserData() {
        tableModel.setRowCount(0);
        List<UserModel> users = userDAO.getAllUsers();
        for (UserModel u : users) tableModel.addRow(new Object[]{u.getUserId(), u.getFullName(), u.getUsername(), u.getRole()});
    }

    private JLabel createStyledLabel(String text) { JLabel lbl = new JLabel(text); lbl.setFont(new Font("Segoe UI", Font.BOLD, 12)); lbl.setForeground(new Color(113, 113, 122)); return lbl; }
}