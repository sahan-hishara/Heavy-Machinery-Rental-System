package views;

import database.ClientDAO;
import database.DatabaseConnection;
import models.ClientModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class ClientManagerPanel extends JPanel {

    private MainFrame mainFrame;
    private ClientDAO clientDAO;
    private JTable clientTable;
    private DefaultTableModel tableModel;
    private JTextField txtCompanyName, txtContactPerson, txtNicNumber, txtBrnNumber, txtTinNumber, txtPhoneNumber, txtInsurancePolicy, txtInsuranceExpiry;

    public ClientManagerPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.clientDAO = new ClientDAO();
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        initComponents();
        loadClientData();
    }

    private void initComponents() {
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Client Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(24, 24, 27));
        
        JLabel lblSub = new JLabel("Register clients and track their insurance and contact details.");
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
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(228, 228, 231), 1, true),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(8, 8, 8, 8);

        JLabel lblFormTitle = new JLabel("Register New Client");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblFormTitle.setForeground(new Color(24, 24, 27));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(5, 8, 15, 8); panel.add(lblFormTitle, gbc);

        gbc.gridwidth = 1; gbc.insets = new Insets(6, 8, 6, 8);
        gbc.gridy = 1; gbc.gridx = 0; panel.add(createStyledLabel("Company Name"), gbc);
        txtCompanyName = new JTextField(15); gbc.gridx = 1; panel.add(txtCompanyName, gbc);

        gbc.gridy = 2; gbc.gridx = 0; panel.add(createStyledLabel("Contact Person *"), gbc);
        txtContactPerson = new JTextField(15); gbc.gridx = 1; panel.add(txtContactPerson, gbc);

        gbc.gridy = 3; gbc.gridx = 0; panel.add(createStyledLabel("Phone *"), gbc);
        txtPhoneNumber = new JTextField(15); gbc.gridx = 1; panel.add(txtPhoneNumber, gbc);

        gbc.gridy = 4; gbc.gridx = 0; panel.add(createStyledLabel("NIC Number"), gbc);
        txtNicNumber = new JTextField(15); gbc.gridx = 1; panel.add(txtNicNumber, gbc);

        gbc.gridy = 5; gbc.gridx = 0; panel.add(createStyledLabel("Business Reg (BRN)"), gbc);
        txtBrnNumber = new JTextField(15); gbc.gridx = 1; panel.add(txtBrnNumber, gbc);

        gbc.gridy = 6; gbc.gridx = 0; panel.add(createStyledLabel("TIN Number"), gbc);
        txtTinNumber = new JTextField(15); gbc.gridx = 1; panel.add(txtTinNumber, gbc);

        gbc.gridy = 7; gbc.gridx = 0; panel.add(createStyledLabel("Insurance Policy"), gbc);
        txtInsurancePolicy = new JTextField(15); gbc.gridx = 1; panel.add(txtInsurancePolicy, gbc);

        gbc.gridy = 8; gbc.gridx = 0; panel.add(createStyledLabel("Expiry (YYYY-MM-DD)"), gbc);
        txtInsuranceExpiry = new JTextField(15); gbc.gridx = 1; panel.add(txtInsuranceExpiry, gbc);

        JButton btnSave = new JButton("Register Client");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setBackground(new Color(59, 130, 246)); btnSave.setForeground(Color.WHITE); 
        btnSave.setPreferredSize(new Dimension(0, 40));
        btnSave.addActionListener(e -> processClientRegistration());
        gbc.gridy = 9; gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(20, 8, 5, 8); panel.add(btnSave, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(228, 228, 231), 1, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        String[] columns = {"ID", "Client Name", "Contact", "Phone"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        
        clientTable = new JTable(tableModel);
        clientTable.setSelectionBackground(new Color(244, 244, 245));
        clientTable.setSelectionForeground(new Color(24, 24, 27));
        clientTable.setShowGrid(false); 
        clientTable.setIntercellSpacing(new Dimension(0, 0));
        
        JTableHeader header = clientTable.getTableHeader();
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(228, 228, 231)));
        
        clientTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(244, 244, 245))); return c;
            }
        });

        clientTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent me) {
                if (me.getClickCount() == 2 && clientTable.getSelectedRow() != -1) {
                    int clientId = (int) tableModel.getValueAt(clientTable.getSelectedRow(), 0);
                    showClientDetails(clientId);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(clientTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        // --- NEW: MULTI-BUTTON CONTROL PANEL ---
        JPanel bottomControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottomControls.setBackground(Color.WHITE);
        bottomControls.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JButton btnUpdateInsurance = new JButton("Update Insurance");
        btnUpdateInsurance.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnUpdateInsurance.setBackground(new Color(245, 158, 11)); 
        btnUpdateInsurance.setForeground(Color.WHITE);
        btnUpdateInsurance.setPreferredSize(new Dimension(160, 40));
        btnUpdateInsurance.addActionListener(e -> updateInsuranceExpiry());
        
        JButton btnDelete = new JButton("Remove Client");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDelete.setBackground(new Color(239, 68, 68)); 
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setPreferredSize(new Dimension(160, 40));
        btnDelete.addActionListener(e -> deleteSelectedClient());
        
        bottomControls.add(btnUpdateInsurance);
        bottomControls.add(btnDelete);
        panel.add(bottomControls, BorderLayout.SOUTH);

        return panel;
    }

    // --- ADD THIS METHOD ANYWHERE INSIDE ClientManagerPanel ---
    private void deleteSelectedClient() {
        int row = clientTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a client to remove."); return; }
        
        int clientId = (int) tableModel.getValueAt(row, 0);
        String clientName = (String) tableModel.getValueAt(row, 1);
        
        if (JOptionPane.showConfirmDialog(this, "Remove " + clientName + " from active clients?", "Confirm Removal", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection(); 
                 PreparedStatement stmt = conn.prepareStatement("UPDATE Clients SET is_active = FALSE WHERE client_id = ?")) {
                stmt.setInt(1, clientId); stmt.executeUpdate(); 
                JOptionPane.showMessageDialog(this, "Client safely archived."); 
                loadClientData(); // Refresh the table
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Database Error."); }
        }
    }

    private void processClientRegistration() {
        if (txtContactPerson.getText().isEmpty() || txtPhoneNumber.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Contact Person and Phone are required."); return;
        }

        ClientModel newClient = new ClientModel();
        newClient.setCompanyName(txtCompanyName.getText()); newClient.setContactPerson(txtContactPerson.getText());
        newClient.setNicNumber(txtNicNumber.getText()); newClient.setBrnNumber(txtBrnNumber.getText());
        newClient.setTinNumber(txtTinNumber.getText()); newClient.setPhoneNumber(txtPhoneNumber.getText());
        newClient.setInsurancePolicy(txtInsurancePolicy.getText());
        try { newClient.setInsuranceExpiry(LocalDate.parse(txtInsuranceExpiry.getText())); } catch(Exception e) {}

        if (clientDAO.addClient(newClient)) {
            JOptionPane.showMessageDialog(this, "Client registered!");
            txtCompanyName.setText(""); txtContactPerson.setText(""); txtPhoneNumber.setText("");
            loadClientData();
        }
    }

    private void updateInsuranceExpiry() {
        int row = clientTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a client from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        
        int clientId = (int) tableModel.getValueAt(row, 0);
        String clientName = (String) tableModel.getValueAt(row, 1);

        SpinnerDateModel dateModel = new SpinnerDateModel(); JSpinner spnDate = new JSpinner(dateModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(spnDate, "yyyy-MM-dd"); spnDate.setEditor(timeEditor);

        JPanel promptPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        promptPanel.add(new JLabel("Select new expiry date for " + clientName + ":"));
        promptPanel.add(spnDate);

        int result = JOptionPane.showConfirmDialog(this, promptPanel, "Renew Insurance", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            java.util.Date selectedDate = (java.util.Date) spnDate.getValue();
            LocalDate newExpiry = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            
            if (clientDAO.updateInsuranceExpiry(clientId, newExpiry)) {
                JOptionPane.showMessageDialog(this, "Insurance expiry updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else { JOptionPane.showMessageDialog(this, "Failed to update insurance.", "Database Error", JOptionPane.ERROR_MESSAGE); }
        }
    }

    private void loadClientData() {
        tableModel.setRowCount(0);
        List<ClientModel> clients = clientDAO.getAllActiveClients();
        for (ClientModel c : clients) {
            tableModel.addRow(new Object[]{c.getClientId(), c.isCorporateClient() ? c.getCompanyName() : c.getContactPerson(), c.getContactPerson(), c.getPhoneNumber()});
        }
    }

    private void showClientDetails(int clientId) {
        JDialog dialog = new JDialog(mainFrame, "Client Details: ID " + clientId, true);
        dialog.setSize(400, 480); dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.setBackground(Color.WHITE); panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String query = "SELECT * FROM Clients WHERE client_id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, clientId); ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                panel.add(new JLabel("<html><b>Company Name:</b> " + (rs.getString("company_name")!=null?rs.getString("company_name"):"N/A") + "</html>"));
                panel.add(new JLabel("<html><b>Contact Person:</b> " + rs.getString("contact_person") + "</html>"));
                panel.add(new JLabel("<html><b>Phone Number:</b> " + rs.getString("phone_number") + "</html>"));
                panel.add(new JLabel("<html><hr></html>"));
                panel.add(new JLabel("<html><b>NIC:</b> " + (rs.getString("nic_number")!=null?rs.getString("nic_number"):"N/A") + "</html>"));
                panel.add(new JLabel("<html><b>BRN (Reg No):</b> " + (rs.getString("brn_number")!=null?rs.getString("brn_number"):"N/A") + "</html>"));
                panel.add(new JLabel("<html><b>TIN (Tax No):</b> " + (rs.getString("tin_number")!=null?rs.getString("tin_number"):"N/A") + "</html>"));
                panel.add(new JLabel("<html><hr></html>"));
                panel.add(new JLabel("<html><b>Insurance Policy:</b> " + (rs.getString("insurance_policy")!=null?rs.getString("insurance_policy"):"None") + "</html>"));
                panel.add(new JLabel("<html><b>Expiry Date:</b> " + (rs.getDate("insurance_expiry")!=null?rs.getDate("insurance_expiry"):"N/A") + "</html>"));
                panel.add(new JLabel("<html><b>Credit Status:</b> <span style='color:#16a34a;'>" + rs.getString("credit_status") + "</span></html>"));
            }
        } catch (Exception e) { e.printStackTrace(); }

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dialog.dispose());
        panel.add(btnClose);
        dialog.add(panel); dialog.setVisible(true);
    }

    private JLabel createStyledLabel(String text) { JLabel lbl = new JLabel(text); lbl.setFont(new Font("Segoe UI", Font.BOLD, 12)); lbl.setForeground(new Color(113, 113, 122)); return lbl; }
}