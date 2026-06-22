package models;

/**
 * Abstract Base Class for the Fleet Inventory.
 * You cannot instantiate this class directly (e.g., new Equipment() is forbidden).
 * You must create a HeavyMachinery or LightTool object instead.
 */
public abstract class Equipment {
    
    // Private attributes for strict encapsulation
    private int equipmentId;
    private String assetTag;
    private String brandModel;
    
    // Protected so subclasses (HeavyMachinery/LightTool) can access it directly for their math
    protected double baseDailyRate; 
    
    private String status;
    private String category;

    /**
     * Full Constructor
     */
    public Equipment(int equipmentId, String assetTag, String brandModel, double baseDailyRate, String status, String category) {
        this.equipmentId = equipmentId;
        this.assetTag = assetTag;
        this.brandModel = brandModel;
        this.baseDailyRate = baseDailyRate;
        this.status = status;
        this.category = category;
    }

    /**
     * Polymorphic Abstract Method:
     * Forces any subclass to define its own specific billing rules.
     */
    public abstract double calculateDailyCost();

    // --- Standard Getters ---
    
    public int getEquipmentId() {
        return equipmentId;
    }

    public String getAssetTag() {
        return assetTag;
    }

    public String getBrandModel() {
        return brandModel;
    }

    public double getBaseDailyRate() {
        return baseDailyRate;
    }

    public String getStatus() {
        return status;
    }

    public String getCategory() {
        return category;
    }

    // --- Sanitizing Setters ---

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public void setAssetTag(String assetTag) {
        // Enforce uppercase asset tags for barcode/database consistency
        this.assetTag = (assetTag != null) ? assetTag.trim().toUpperCase() : null;
    }

    public void setBrandModel(String brandModel) {
        this.brandModel = brandModel;
    }

    public void setBaseDailyRate(double baseDailyRate) {
        // Prevent negative rental rates
        if (baseDailyRate >= 0) {
            this.baseDailyRate = baseDailyRate;
        } else {
            System.err.println("Error: Base daily rate cannot be negative.");
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}