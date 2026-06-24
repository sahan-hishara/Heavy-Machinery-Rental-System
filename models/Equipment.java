package models;

public abstract class Equipment {
    
    private int equipmentId;
    private String assetTag;
    private String brandModel;
    protected double baseDailyRate; 
    private String status;
    private String category;
    
    public Equipment(int equipmentId, String assetTag, String brandModel, double baseDailyRate, String status, String category) {
        this.equipmentId = equipmentId;
        this.assetTag = assetTag;
        this.brandModel = brandModel;
        this.baseDailyRate = baseDailyRate;
        this.status = status;
        this.category = category;
    }

    public abstract double calculateDailyCost();
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


    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public void setAssetTag(String assetTag) {
        this.assetTag = (assetTag != null) ? assetTag.trim().toUpperCase() : null;
    }

    public void setBrandModel(String brandModel) {
        this.brandModel = brandModel;
    }

    public void setBaseDailyRate(double baseDailyRate) {
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
