package tr.edu.traffic.model;

public class StandardVehicle extends Vehicle {

    public StandardVehicle(String licensePlate, String vehicleCategory, double averageSpeedKmh) {
        super(licensePlate, vehicleCategory, averageSpeedKmh);
    }

    @Override
    public double getTrafficPenaltyMultiplier() {
        return 2.5; // Yoğunluktan yüksek oranda etkilenir, algoritma boş yolları tercih eder
    }

    @Override
    public boolean hasEmergencyPriority() {
        return false;
    }
}