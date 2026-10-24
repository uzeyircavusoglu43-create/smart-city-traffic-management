package tr.edu.traffic.model;

import java.util.Objects;

public class Intersection implements TrafficObserver {
    private final String id;
    private String name;
    private TrafficLightState lightState;

    public Intersection(String id, String name) {
        if (id == null || id.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Kavşak ID ve ismi boş olamaz!");
        }
        this.id = id.trim();
        this.name = name.trim();
        this.lightState = TrafficLightState.RED; // Varsayılan başlangıç
    }

    @Override
    public void onEmergencyAlert(String emergencyVehiclePlate, boolean isActive) {
        if (isActive) {
            this.lightState = TrafficLightState.EMERGENCY_GREEN;
            System.out.println("[BİLDİRİM] " + name + " (" + id + ") kavşağı "
                    + emergencyVehiclePlate + " plakalı acil araç için YEŞİL DALGA moduna geçti!");
        } else {
            this.lightState = TrafficLightState.GREEN;
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public TrafficLightState getLightState() { return lightState; }
    public void setLightState(TrafficLightState lightState) { this.lightState = lightState; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Intersection that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " [" + id + " - Işık: " + lightState + "]";
    }
}