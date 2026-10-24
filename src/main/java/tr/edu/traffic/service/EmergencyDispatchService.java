package tr.edu.traffic.service;

import tr.edu.traffic.model.Intersection;
import tr.edu.traffic.model.TrafficObserver;
import tr.edu.traffic.model.Vehicle;
import tr.edu.traffic.strategy.RouteResult;
import tr.edu.traffic.strategy.RouteStrategy;

import java.util.List;

public class EmergencyDispatchService {
    private RouteStrategy routeStrategy;

    // Dependency Inversion: Somut sınıfa değil, RouteStrategy arayüzüne bağımlıdır
    public EmergencyDispatchService(RouteStrategy routeStrategy) {
        this.routeStrategy = routeStrategy;
    }

    public void setRouteStrategy(RouteStrategy routeStrategy) {
        this.routeStrategy = routeStrategy;
    }

    // Acil aracı sevk eder ve güzergahtaki kavşakları (Observer) uyarır
    public RouteResult dispatchEmergencyVehicle(CityMap cityMap, Intersection start, Intersection target, Vehicle vehicle) {
        if (!vehicle.hasEmergencyPriority()) {
            throw new IllegalArgumentException("Bu servis sadece acil durum önceliği olan araçlar içindir!");
        }

        RouteResult result = routeStrategy.calculateRoute(cityMap, start, target, vehicle);

        if (result.isRouteFound()) {
            System.out.println("\n>>> ACİL DURUM PROTOKOLÜ BAŞLATILDI: " + vehicle.getLicensePlate() + " <<<");
            notifyIntersectionsOnRoute(result.getPath(), vehicle.getLicensePlate(), true);
        }

        return result;
    }

    // Observer Pattern: Güzergah üzerindeki kavşaklara bildirim gönderir
    private void notifyIntersectionsOnRoute(List<Intersection> path, String plate, boolean isActive) {
        for (TrafficObserver observer : path) {
            observer.onEmergencyAlert(plate, isActive);
        }
    }
}