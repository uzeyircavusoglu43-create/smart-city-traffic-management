package tr.edu.traffic.strategy;

import tr.edu.traffic.model.Intersection;

import java.util.List;
import java.util.stream.Collectors;

public class RouteResult {
    private final List<Intersection> path;
    private final double totalDistanceKm;
    private final double estimatedCost;
    private final String strategyName;

    public RouteResult(List<Intersection> path, double totalDistanceKm, double estimatedCost, String strategyName) {
        this.path = path;
        this.totalDistanceKm = totalDistanceKm;
        this.estimatedCost = estimatedCost;
        this.strategyName = strategyName;
    }

    public boolean isRouteFound() {
        return path != null && !path.isEmpty();
    }

    public List<Intersection> getPath() { return path; }
    public double getTotalDistanceKm() { return totalDistanceKm; }
    public double getEstimatedCost() { return estimatedCost; }
    public String getStrategyName() { return strategyName; }

    @Override
    public String toString() {
        if (!isRouteFound()) {
            return "Bu iki kavşak arasında uygun bir rota bulunamadı!";
        }
        String routeStr = path.stream()
                .map(i -> i.getName() + " (" + i.getId() + ")")
                .collect(Collectors.joining(" -> "));

        return String.format("""
                --- ROTA HESAPLAMA SONUCU [%s] ---
                Güzergah        : %s
                Toplam Mesafe   : %.2f km
                Trafik Maliyeti : %.2f birim
                """, strategyName, routeStr, totalDistanceKm, estimatedCost);
    }
}