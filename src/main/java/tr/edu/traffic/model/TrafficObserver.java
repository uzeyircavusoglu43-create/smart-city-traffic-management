package tr.edu.traffic.model;

public interface TrafficObserver {
    void onEmergencyAlert(String emergencyVehiclePlate, boolean isActive);
}