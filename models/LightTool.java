package models;

public class LightTool extends Equipment {
    
    private boolean requiresCleaning;
    private int quantityAvailable;

    public LightTool(int equipmentId, String assetTag, String brandModel, double baseDailyRate, 
                     String status, boolean requiresCleaning, int quantityAvailable) {
        super(equipmentId, assetTag, brandModel, baseDailyRate, status, "Light");
        this.requiresCleaning = requiresCleaning;
        this.quantityAvailable = quantityAvailable;
    }

    @Override
    public double calculateDailyCost() {
        return this.baseDailyRate;
    }

    
    public boolean isRequiresCleaning() {
        return requiresCleaning;
    }

    public void setRequiresCleaning(boolean requiresCleaning) {
        this.requiresCleaning = requiresCleaning;
    }

    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(int quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }
}
