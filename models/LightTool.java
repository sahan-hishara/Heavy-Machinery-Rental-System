package models;

public class LightTool extends Equipment {
    
    private boolean requiresCleaning;
    private int quantityAvailable;

    /**
     * Full Constructor for Light Tools
     */
    public LightTool(int equipmentId, String assetTag, String brandModel, double baseDailyRate, 
                     String status, boolean requiresCleaning, int quantityAvailable) {
        // Call the abstract parent constructor, hardcoding the category to "Light"
        super(equipmentId, assetTag, brandModel, baseDailyRate, status, "Light");
        this.requiresCleaning = requiresCleaning;
        this.quantityAvailable = quantityAvailable;
    }

    /**
     * Polymorphic Billing Logic:
     * Unlike Heavy Machinery, Light Tools don't require an operator fee or engine hours math.
     * It simply returns the base flat rate.
     */
    @Override
    public double calculateDailyCost() {
        return this.baseDailyRate;
    }

    // --- Getters and Setters ---
    
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