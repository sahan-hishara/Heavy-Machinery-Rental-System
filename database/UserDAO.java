package database;

import models.UserModel;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    /**
     * Authenticates a user securely using Bcrypt hash verification.
     */
    public UserModel authenticateUser(String username, String enteredPassword) {
        String query = "SELECT * FROM System_Users WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
             
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                
                // Compare the entered password against the stored database hash
                if (BCrypt.checkpw(enteredPassword, storedHash)) {
                    UserModel user = new UserModel();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setFullName(rs.getString("full_name"));
                    user.setRole(rs.getString("role"));
                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Login failed
    }

    /**
     * Adds a new system user. Automatically hashes the password before saving.
     */
    public boolean addUser(UserModel user, String plainTextPassword) {
        String query = "INSERT INTO System_Users (username, password_hash, full_name, role) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
             
            // Generate a secure salt and hash the password
            String hashedPassword = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(10));
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, hashedPassword); // Save the HASH, never the plain text!
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getRole());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding user (Username might already exist).");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a list of all active users for the Admin User Management panel.
     */
    public java.util.List<UserModel> getAllUsers() {
        java.util.List<UserModel> users = new java.util.ArrayList<>();
        String query = "SELECT user_id, username, full_name, role FROM System_Users WHERE is_active = TRUE";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                UserModel user = new UserModel(); user.setUserId(rs.getInt("user_id")); user.setUsername(rs.getString("username")); user.setFullName(rs.getString("full_name")); user.setRole(rs.getString("role")); users.add(user);
            }
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
        return users;
    }

    public boolean deleteUser(int userId) {
        String query = "UPDATE System_Users SET is_active = FALSE WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId); return stmt.executeUpdate() > 0;
        } catch (java.sql.SQLException e) { e.printStackTrace(); return false; }
    }
}