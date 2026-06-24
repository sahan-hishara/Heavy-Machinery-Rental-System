package models;

public class UserModel {
    private int userId;
    private String username;
    private String fullName;
    private String role; 

    public UserModel() {}

    public UserModel(String username, String fullName, String role) {
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }
    public UserModel(int userId, String username, String fullName, String role) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public boolean isAdmin() { return "Admin".equals(this.role); }
    public boolean isDeskClerk() { return "Desk Clerk".equals(this.role); }
}
