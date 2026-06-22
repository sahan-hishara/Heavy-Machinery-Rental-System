package views;

import database.UserDAO;
import models.UserModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginPanel extends JPanel {

    private MainFrame mainFrame;
    private UserDAO userDAO;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userDAO = new UserDAO();
        
        setLayout(new GridBagLayout());
        setBackground(new Color(243, 244, 246)); // Very light gray background
        initComponents();
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1), // Subtle border
                BorderFactory.createEmptyBorder(50, 60, 50, 60)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        JLabel lblTitle = new JLabel("Welcome Back");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(17, 24, 39));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 30, 0);
        formPanel.add(lblTitle, gbc);

        gbc.insets = new Insets(5, 0, 5, 0);
        JLabel lblUsername = new JLabel("Username");
        lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUsername.setForeground(new Color(107, 114, 128));
        gbc.gridy = 1;
        formPanel.add(lblUsername, gbc);

        txtUsername = createStyledTextField();
        gbc.gridy = 2;
        formPanel.add(txtUsername, gbc);

        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPassword.setForeground(new Color(107, 114, 128));
        gbc.gridy = 3;
        gbc.insets = new Insets(15, 0, 5, 0);
        formPanel.add(lblPassword, gbc);

        txtPassword = new JPasswordField(20);
        styleTextField(txtPassword);
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 0, 5, 0);
        formPanel.add(txtPassword, gbc);

        btnLogin = new JButton("Sign In");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(79, 70, 229)); // Modern Indigo
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setPreferredSize(new Dimension(0, 40));
        
        gbc.gridy = 5;
        gbc.insets = new Insets(30, 0, 0, 0);
        formPanel.add(btnLogin, gbc);

        btnLogin.addActionListener((ActionEvent e) -> performLogin());

        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) performLogin(); }
        };
        txtUsername.addKeyListener(enterKey);
        txtPassword.addKeyListener(enterKey);

        add(formPanel);
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField(20);
        styleTextField(tf);
        return tf;
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = String.valueOf(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        UserModel authenticatedUser = userDAO.authenticateUser(username, password);
        setCursor(Cursor.getDefaultCursor());

        if (authenticatedUser != null) {
            txtUsername.setText(""); txtPassword.setText("");
            mainFrame.loginSuccess(authenticatedUser);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText(""); txtPassword.requestFocus();
        }
    }
}