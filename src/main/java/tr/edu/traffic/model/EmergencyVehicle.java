package tr.edu.traffic.model;

public class EmergencyVehicle extends Vehicle {

    public EmergencyVehicle(String licensePlate, String emergencyType, double averageSpeedKmh) {
        super(licensePlate, "ACİL - " + emergencyType, averageSpeedKmh);
    }

    @Override
    public double getTrafficPenaltyMultiplier() {
        return 0.15; // Emniyet şeridi ve yeşil dalga sayesinde trafikten çok az etkilenir
    }

    @Override
    public boolean hasEmergencyPriority() {
        return true;
    }
}