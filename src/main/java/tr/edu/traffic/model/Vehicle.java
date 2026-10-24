package tr.edu.traffic.model;

public abstract class Vehicle {
    private final String licensePlate;
    private final String typeName;
    private double averageSpeedKmh;

    protected Vehicle(String licensePlate, String typeName, double averageSpeedKmh) {
        if (licensePlate == null || licensePlate.isBlank()) {
            throw new IllegalArgumentException("Plaka boş olamaz!");
        }
        if (averageSpeedKmh <= 0) {
            throw new IllegalArgumentException("Hız 0'dan büyük olmalıdır!");
        }
        this.licensePlate = licensePlate.trim().toUpperCase();
        this.typeName = typeName;
        this.averageSpeedKmh = averageSpeedKmh;
    }

    // Polymorphism): Her araç tipi trafikten farklı oranda etkilenir
    public abstract double getTrafficPenaltyMultiplier();

    // Acil önceliği olup olmadığını belirtir
    public abstract boolean hasEmergencyPriority();

    public String getLicensePlate() { return licensePlate; }
    public String getTypeName() { return typeName; }
    public double getAverageSpeedKmh() { return averageSpeedKmh; }
    public void setAverageSpeedKmh(double averageSpeedKmh) { this.averageSpeedKmh = averageSpeedKmh; }

    @Override
    public String toString() {
        return String.format("[%s] Plaka: %s | Ort. Hız: %.0f km/s | Acil Öncelik: %s",
                typeName, licensePlate, averageSpeedKmh, hasEmergencyPriority() ? "EVET" : "HAYIR");
    }
}