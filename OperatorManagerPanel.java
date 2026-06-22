package views;

import database.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class OperatorManagerPanel extends JPanel {

    private MainFrame mainFrame;
    private JTable operatorTable;
    private DefaultTableModel tableModel;
    private JTextField txtFullName, txtDailyWage;
    private JCheckBox chkExcavator, chkBackhoe, chkCrane, chkRoller;

    public OperatorManagerPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        initComponents();
        loadOperatorData();
    }

    private void initComponents() {
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Operator & Crew Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(24, 24, 27));
        JLabel lblSub = new JLabel("Manage operator wages, specializations, and employment status.");
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
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(228, 228, 231), 1, true), BorderFactory.createEmptyBorder(20, 25, 20, 25)));

        GridBagConstraints gbc = new GridBagConstraints(); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(8, 8, 8, 8);

        JLabel lblFormTitle = new JLabel("Register Operator"); lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblFormTitle.setForeground(new Color(24, 24, 27));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(5, 8, 15, 8); panel.add(lblFormTitle, gbc);

        gbc.gridwidth = 1; gbc.insets = new Insets(6, 8, 6, 8);
        gbc.gridy = 1; gbc.gridx = 0; panel.add(createStyledLabel("Full Name *"), gbc); txtFullName = new JTextField(15); gbc.gridx = 1; panel.add(txtFullName, gbc);

        gbc.gridy = 2; gbc.gridx = 0; panel.add(createStyledLabel("Certified Vehicles"), gbc);
        JPanel pnlSpecs = new JPanel(new GridLayout(2, 2, 5, 5)); pnlSpecs.setOpaque(false);
        chkExcavator = new JCheckBox("Excavator"); chkExcavator.setOpaque(false);
        chkBackhoe = new JCheckBox("Backhoe"); chkBackhoe.setOpaque(false);
        chkCrane = new JCheckBox("Crane"); chkCrane.setOpaque(false);
        chkRoller = new JCheckBox("Roller"); chkRoller.setOpaque(false);
        pnlSpecs.add(chkExcavator); pnlSpecs.add(chkBackhoe); pnlSpecs.add(chkCrane); pnlSpecs.add(chkRoller);
        gbc.gridx = 1; panel.add(pnlSpecs, gbc);

        gbc.gridy = 3; gbc.gridx = 0; panel.add(createStyledLabel("Base Daily Wage *"), gbc);
        txtDailyWage = new JTextField(15); txtDailyWage.setText("4000.00"); gbc.gridx = 1; panel.add(txtDailyWage, gbc);

        JButton btnSave = new JButton("Add Operator");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14)); btnSave.setBackground(new Color(59, 130, 246)); btnSave.setForeground(Color.WHITE); btnSave.setPreferredSize(new Dimension(0, 40));
        btnSave.addActionListener(e -> saveOperator());
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(20, 8, 5, 8); panel.add(btnSave, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(228, 228, 231), 1, true), BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        String[] cols = {"ID", "Name", "Specializations", "Daily Wage", "Status"};
        tableModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        operatorTable = new JTable(tableModel);
        operatorTable.setSelectionBackground(new Color(244, 244, 245)); operatorTable.setShowGrid(false); operatorTable.setIntercellSpacing(new Dimension(0, 0));
        
        operatorTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(228, 228, 231)));
        operatorTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(244, 244, 245))); return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(operatorTable); scrollPane.setBorder(BorderFactory.createEmptyBorder()); scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        // --- NEW: EDIT & DELETE CONTROLS ---
        JPanel bottomControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottomControls.setBackground(Color.WHITE); bottomControls.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JButton btnEdit = new JButton("Edit Selected Operator");
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 13)); btnEdit.setBackground(new Color(245, 158, 11)); btnEdit.setForeground(Color.WHITE); btnEdit.setPreferredSize(new Dimension(200, 40));
        btnEdit.addActionListener(e -> updateSelectedOperator());

        JButton btnDelete = new JButton("Remove Operator");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 13)); btnDelete.setBackground(new Color(239, 68, 68)); btnDelete.setForeground(Color.WHITE); btnDelete.setPreferredSize(new Dimension(180, 40));
        btnDelete.addActionListener(e -> deleteSelectedOperator());
        
        bottomControls.add(btnEdit); bottomControls.add(btnDelete);
        panel.add(bottomControls, BorderLayout.SOUTH);

        return panel;
    }

    private void saveOperator() {
        if (txtFullName.getText().isEmpty() || txtDailyWage.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Name and Wage are required."); return; }
        List<String> specsList = new ArrayList<>();
        if (chkExcavator.isSelected()) specsList.add("Excavator"); if (chkBackhoe.isSelected()) specsList.add("Backhoe");
        if (chkCrane.isSelected()) specsList.add("Crane"); if (chkRoller.isSelected()) specsList.add("Roller");
        String specs = specsList.isEmpty() ? "All" : String.join(", ", specsList);
        
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement("INSERT INTO Operators (full_name, specializations, daily_wage, status) VALUES (?, ?, ?, 'Available')")) {
            stmt.setString(1, txtFullName.getText()); stmt.setString(2, specs); stmt.setDouble(3, Double.parseDouble(txtDailyWage.getText()));
            stmt.executeUpdate(); JOptionPane.showMessageDialog(this, "Operator added successfully.");
            txtFullName.setText(""); txtDailyWage.setText("4000.00");
            chkExcavator.setSelected(false); chkBackhoe.setSelected(false); chkCrane.setSelected(false); chkRoller.setSelected(false);
            loadOperatorData();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Database Error.", "Error", JOptionPane.ERROR_MESSAGE); }
    }

    // --- NEW: EDIT OPERATOR LOGIC ---
    private void updateSelectedOperator() {
        int row = operatorTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select an operator to edit."); return; }
        
        int opId = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String wageStr = tableModel.getValueAt(row, 3).toString().replace("Rs. ", "");

        JPanel promptPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        promptPanel.add(new JLabel("Update details for " + name + ":"));
        
        JTextField txtNewSpecs = new JTextField((String) tableModel.getValueAt(row, 2));
        promptPanel.add(new JLabel("Specializations (Comma separated):")); promptPanel.add(txtNewSpecs);
        
        JTextField txtNewWage = new JTextField(wageStr);
        promptPanel.add(new JLabel("New Daily Wage (Rs):")); promptPanel.add(txtNewWage);

        if (JOptionPane.showConfirmDialog(this, promptPanel, "Edit Operator", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection(); 
                 PreparedStatement stmt = conn.prepareStatement("UPDATE Operators SET specializations = ?, daily_wage = ? WHERE operator_id = ?")) {
                stmt.setString(1, txtNewSpecs.getText()); stmt.setDouble(2, Double.parseDouble(txtNewWage.getText())); stmt.setInt(3, opId);
                stmt.executeUpdate(); JOptionPane.showMessageDialog(this, "Operator updated successfully."); loadOperatorData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid Data."); }
        }
    }

    // --- NEW: SOFT DELETE OPERATOR LOGIC ---
    private void deleteSelectedOperator() {
        int row = operatorTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select an operator to remove."); return; }
        
        int opId = (int) tableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this operator?", "Confirm Removal", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection(); 
                 PreparedStatement stmt = conn.prepareStatement("UPDATE Operators SET status = 'Retired' WHERE operator_id = ?")) {
                stmt.setInt(1, opId); stmt.executeUpdate(); 
                JOptionPane.showMessageDialog(this, "Operator safely archived."); loadOperatorData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Database Error."); }
        }
    }

    public void loadOperatorData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Operators WHERE status != 'Retired'"); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) tableModel.addRow(new Object[]{rs.getInt("operator_id"), rs.getString("full_name"), rs.getString("specializations"), "Rs. " + rs.getDouble("daily_wage"), rs.getString("status")});
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JLabel createStyledLabel(String text) { JLabel lbl = new JLabel(text); lbl.setFont(new Font("Segoe UI", Font.BOLD, 12)); lbl.setForeground(new Color(113, 113, 122)); return lbl; }
}