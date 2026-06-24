package database;

import models.ClientModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    public boolean addClient(ClientModel client) {
        String query = "INSERT INTO Clients (company_name, contact_person, nic_number, brn_number, " +
                       "tin_number, phone_number, insurance_policy, insurance_expiry, credit_status, is_active) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, client.getCompanyName());
            stmt.setString(2, client.getContactPerson());
            stmt.setString(3, client.getNicNumber());
            stmt.setString(4, client.getBrnNumber());
            stmt.setString(5, client.getTinNumber());
            stmt.setString(6, client.getPhoneNumber());
            stmt.setString(7, client.getInsurancePolicy());
            stmt.setDate(8, Date.valueOf(client.getInsuranceExpiry()));
            stmt.setString(9, client.getCreditStatus());
            stmt.setBoolean(10, client.isActive());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error adding client: " + e.getMessage());
            return false;
        }
    }

    //Update Insuarance Expiry Date
    public boolean updateInsuranceExpiry(int clientId, java.time.LocalDate newExpiry) {
        String query = "UPDATE Clients SET insurance_expiry = ? WHERE client_id = ?";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(newExpiry));
            stmt.setInt(2, clientId);
            return stmt.executeUpdate() > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ClientModel> getAllActiveClients() {
        List<ClientModel> clientList = new ArrayList<>();
        String query = "SELECT * FROM Clients WHERE is_active = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Date sqlDate = rs.getDate("insurance_expiry");
                LocalDate expiryDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                ClientModel client = new ClientModel(
                    rs.getInt("client_id"),
                    rs.getString("company_name"),
                    rs.getString("contact_person"),
                    rs.getString("nic_number"),
                    rs.getString("brn_number"),
                    rs.getString("tin_number"),
                    rs.getString("phone_number"),
                    rs.getString("insurance_policy"),
                    expiryDate,
                    rs.getString("credit_status"),
                    rs.getBoolean("is_active")
                );
                
                clientList.add(client);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching clients: " + e.getMessage());
        }
        return clientList;
    }

    //Updates client details
    public boolean updateClientInsurance(int clientId, String newPolicyNumber, LocalDate newExpiryDate) {
        String query = "UPDATE Clients SET insurance_policy = ?, insurance_expiry = ? WHERE client_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newPolicyNumber);
            stmt.setDate(2, Date.valueOf(newExpiryDate));
            stmt.setInt(3, clientId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating client insurance: " + e.getMessage());
            return false;
        }
    }

    //Prevents Client duplication via NIC and BRN
    public boolean isClientExists(String nicNumber, String brnNumber) {
        boolean hasNic = (nicNumber != null && !nicNumber.trim().isEmpty());
        boolean hasBrn = (brnNumber != null && !brnNumber.trim().isEmpty());

        if (!hasNic && !hasBrn) return false; 

        StringBuilder query = new StringBuilder("SELECT client_id FROM Clients WHERE ");
        if (hasNic && hasBrn) query.append("nic_number = ? OR brn_number = ?");
        else if (hasNic) query.append("nic_number = ?");
        else query.append("brn_number = ?");
        
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query.toString())) {
            if (hasNic && hasBrn) { stmt.setString(1, nicNumber); stmt.setString(2, brnNumber); } 
            else if (hasNic) stmt.setString(1, nicNumber); 
            else stmt.setString(1, brnNumber);
            try (ResultSet rs = stmt.executeQuery()) { return rs.next(); }
        } catch (java.sql.SQLException e) { return false; }
    }
}
