package database;

import models.Equipment;
import models.HeavyMachinery;
import models.LightTool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EquipmentDAO {

    //Adding new Equipment
    public boolean addEquipment(Equipment newEquipment) {
        String insertBaseQuery = "INSERT INTO Equipment (asset_tag, brand_model, base_daily_rate, status, category) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement baseStmt = null;
        PreparedStatement subStmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); 

            baseStmt = conn.prepareStatement(insertBaseQuery, Statement.RETURN_GENERATED_KEYS);
            baseStmt.setString(1, newEquipment.getAssetTag());
            baseStmt.setString(2, newEquipment.getBrandModel());
            baseStmt.setDouble(3, newEquipment.getBaseDailyRate());
            baseStmt.setString(4, newEquipment.getStatus());
            baseStmt.setString(5, newEquipment.getCategory());
            
            int affectedRows = baseStmt.executeUpdate();

            if (affectedRows == 0) throw new SQLException("Creating equipment base record failed, no rows affected.");

            generatedKeys = baseStmt.getGeneratedKeys();
            int newEquipmentId = -1;
            if (generatedKeys.next()) {
                newEquipmentId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating equipment failed, no ID obtained.");
            }

            if (newEquipment instanceof HeavyMachinery) {
                HeavyMachinery heavy = (HeavyMachinery) newEquipment;
                String insertHeavyQuery = "INSERT INTO Heavy_Machinery (equipment_id, current_engine_hours, requires_operator, operator_wage, ot_rate) VALUES (?, ?, ?, ?, ?)";
                subStmt = conn.prepareStatement(insertHeavyQuery);
                subStmt.setInt(1, newEquipmentId);
                subStmt.setDouble(2, heavy.getCurrentEngineHours());
                subStmt.setBoolean(3, heavy.isRequiresOperator());
                subStmt.setDouble(4, heavy.getOperatorWage());
                subStmt.setDouble(5, heavy.getOtRate());
                subStmt.executeUpdate();
                
            } else if (newEquipment instanceof LightTool) {
                LightTool light = (LightTool) newEquipment;
                String insertLightQuery = "INSERT INTO Light_Tools (equipment_id, requires_cleaning, quantity_available) VALUES (?, ?, ?)";
                subStmt = conn.prepareStatement(insertLightQuery);
                subStmt.setInt(1, newEquipmentId);
                subStmt.setBoolean(2, light.isRequiresCleaning());
                subStmt.setInt(3, light.getQuantityAvailable());
                subStmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
            
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (baseStmt != null) baseStmt.close();
                if (subStmt != null) subStmt.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }
}
