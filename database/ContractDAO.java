package database;

import models.RentalContractModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

public class ContractDAO {

    //Create NEW contract
    public boolean createNewContract(RentalContractModel contract, double advancePay) {
        String insertContract = "INSERT INTO Rental_Contracts (client_id, equipment_id, operator_id, issued_by_user, is_wet_hire, issue_date, expected_return, start_meter, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'Active')";
        String insertInvoice = "INSERT INTO Invoices (contract_id, advance_paid, status) VALUES (?, ?, 'Partial')";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection(); conn.setAutoCommit(false);
            int contractId = 0;
            
            try (PreparedStatement psContract = conn.prepareStatement(insertContract, Statement.RETURN_GENERATED_KEYS)) {
                psContract.setInt(1, contract.getClient().getClientId());
                psContract.setInt(2, contract.getEquipment().getEquipmentId());
                if (contract.isWetHire() && contract.getOperator() != null) psContract.setInt(3, contract.getOperator().getOperatorId());
                else psContract.setNull(3, java.sql.Types.INTEGER);
                
                psContract.setInt(4, contract.getIssuedBy() != null ? contract.getIssuedBy().getUserId() : 1);
                psContract.setBoolean(5, contract.isWetHire());
                psContract.setTimestamp(6, Timestamp.valueOf(contract.getIssueDate()));
                psContract.setTimestamp(7, Timestamp.valueOf(contract.getExpectedReturn()));
                psContract.setDouble(8, contract.getStartMeter());
                psContract.executeUpdate();

                ResultSet rs = psContract.getGeneratedKeys();
                if (rs.next()) contractId = rs.getInt(1);
            }

            if (contractId > 0) {
                contract.setContractId(contractId);
                try (PreparedStatement psInvoice = conn.prepareStatement(insertInvoice)) { psInvoice.setInt(1, contractId); psInvoice.setDouble(2, advancePay); psInvoice.executeUpdate(); }

                if ("Light Tool".equalsIgnoreCase(contract.getEquipment().getCategory())) {
                    try (PreparedStatement psDec = conn.prepareStatement("UPDATE Light_Tools SET quantity_available = quantity_available - 1 WHERE equipment_id = ?")) { psDec.setInt(1, contract.getEquipment().getEquipmentId()); psDec.executeUpdate(); }
                    int remaining = -1;
                    try (PreparedStatement psCheck = conn.prepareStatement("SELECT quantity_available FROM Light_Tools WHERE equipment_id = ?")) {
                        psCheck.setInt(1, contract.getEquipment().getEquipmentId()); ResultSet rs = psCheck.executeQuery(); if (rs.next()) remaining = rs.getInt(1);
                    }
                    if (remaining == 0) {
                        try (PreparedStatement psStatus = conn.prepareStatement("UPDATE Equipment SET status = 'Rented' WHERE equipment_id = ?")) { psStatus.setInt(1, contract.getEquipment().getEquipmentId()); psStatus.executeUpdate(); }
                    }
                } else {
                    try (PreparedStatement psEq = conn.prepareStatement("UPDATE Equipment SET status = 'Rented' WHERE equipment_id = ?")) { psEq.setInt(1, contract.getEquipment().getEquipmentId()); psEq.executeUpdate(); }
                }

                if (contract.isWetHire() && contract.getOperator() != null) {
                    try (PreparedStatement psOp = conn.prepareStatement("UPDATE Operators SET status = 'On_Job' WHERE operator_id = ?")) { psOp.setInt(1, contract.getOperator().getOperatorId()); psOp.executeUpdate(); }
                }
            }
            conn.commit(); return true;
        } catch (Exception e) { if (conn != null) try { conn.rollback(); } catch (Exception ex) {} e.printStackTrace(); return false;
        } finally { if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (Exception ex) {} } }
    }

    public boolean completeContract(int contractId, int equipmentId, int operatorId, boolean isWetHire, double endMeter, Timestamp actualReturn, double fuelSurcharge, double damagePenalty, double overtimeCharge) { 
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection(); conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement("UPDATE Rental_Contracts SET status = 'Completed', actual_return = ?, end_meter = ? WHERE contract_id = ?")) { ps1.setTimestamp(1, actualReturn); ps1.setDouble(2, endMeter); ps1.setInt(3, contractId); ps1.executeUpdate(); }

            String category = "Heavy Machinery"; 
            try (PreparedStatement psCat = conn.prepareStatement("SELECT category FROM Equipment WHERE equipment_id = ?")) { psCat.setInt(1, equipmentId); ResultSet rs = psCat.executeQuery(); if (rs.next()) category = rs.getString(1); }

            if ("Light Tool".equalsIgnoreCase(category)) {
                try (PreparedStatement psInc = conn.prepareStatement("UPDATE Light_Tools SET quantity_available = quantity_available + 1 WHERE equipment_id = ?")) { psInc.setInt(1, equipmentId); psInc.executeUpdate(); }
                try (PreparedStatement psAvail = conn.prepareStatement("UPDATE Equipment SET status = 'Available' WHERE equipment_id = ?")) { psAvail.setInt(1, equipmentId); psAvail.executeUpdate(); }
            } else {
                try (PreparedStatement psEq = conn.prepareStatement("UPDATE Equipment SET status = 'Available' WHERE equipment_id = ?")) { psEq.setInt(1, equipmentId); psEq.executeUpdate(); }
                try (PreparedStatement psMeter = conn.prepareStatement("UPDATE Heavy_Machinery SET current_engine_hours = ? WHERE equipment_id = ?")) { psMeter.setDouble(1, endMeter); psMeter.setInt(2, equipmentId); psMeter.executeUpdate(); }
            }

            if (isWetHire && operatorId > 0) { try (PreparedStatement ps3 = conn.prepareStatement("UPDATE Operators SET status = 'Available' WHERE operator_id = ?")) { ps3.setInt(1, operatorId); ps3.executeUpdate(); } }

            try (PreparedStatement ps4 = conn.prepareStatement("UPDATE Invoices SET fuel_surcharge = ?, damage_penalty = ?, overtime_charge = ?, status = 'Final' WHERE contract_id = ?")) { ps4.setDouble(1, fuelSurcharge); ps4.setDouble(2, damagePenalty); ps4.setDouble(3, overtimeCharge); ps4.setInt(4, contractId); ps4.executeUpdate(); }
            conn.commit(); return true;
        } catch (Exception e) { if (conn != null) try { conn.rollback(); } catch (Exception ex) {} e.printStackTrace(); return false;
        } finally { if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (Exception ex) {} } }
    }
}
