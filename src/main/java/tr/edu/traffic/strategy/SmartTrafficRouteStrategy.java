package tr.edu.traffic.strategy;

import tr.edu.traffic.model.Intersection;
import tr.edu.traffic.model.Road;
import tr.edu.traffic.model.Vehicle;
import tr.edu.traffic.service.CityMap;

import java.util.*;

public class SmartTrafficRouteStrategy implements RouteStrategy {
    private final boolean considerTraffic;

    // considerTraffic = true ise Trafik Yoğunluğu + Acil Önceliği hesaba katar
    // considerTraffic = false ise sadece fiziksel mesafeye (km) bakar
    public SmartTrafficRouteStrategy(boolean considerTraffic) {
        this.considerTraffic = considerTraffic;
    }

    @Override
    public RouteResult calculateRoute(CityMap cityMap, Intersection start, Intersection target, Vehicle vehicle) {
        if (start == null || target == null || vehicle == null) {
            throw new IllegalArgumentException("Başlangıç, hedef ve araç bilgileri boş olamaz!");
        }

        Map<Intersection, Double> minCosts = new HashMap<>();
        Map<Intersection, Double> actualDistances = new HashMap<>();
        Map<Intersection, Intersection> previousNodes = new HashMap<>();
        Set<Intersection> visited = new HashSet<>();

        PriorityQueue<Intersection> queue = new PriorityQueue<>(
                Comparator.comparingDouble(i -> minCosts.getOrDefault(i, Double.MAX_VALUE))
        );

        for (Intersection intersection : cityMap.getAllIntersections()) {
            minCosts.put(intersection, Double.MAX_VALUE);
            actualDistances.put(intersection, 0.0);
        }

        minCosts.put(start, 0.0);
        queue.add(start);

        while (!queue.isEmpty()) {
            Intersection current = queue.poll();

            if (current.equals(target)) {
                break; // Hedefe ulaşıldı
            }

            if (!visited.add(current)) {
                continue;
            }

            for (Road road : cityMap.getRoadsFrom(current)) {
                Intersection neighbor = road.getDestination();
                if (visited.contains(neighbor)) continue;

                // YOL MALİYETİ HESABI (Dinamik Ağırlık)
                double edgeCost = calculateRoadCost(road, vehicle);
                double newCost = minCosts.get(current) + edgeCost;

                if (newCost < minCosts.get(neighbor)) {
                    minCosts.put(neighbor, newCost);
                    actualDistances.put(neighbor, actualDistances.get(current) + road.getDistanceKm());
                    previousNodes.put(neighbor, current);
                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        // Hedefe ulaşılamadıysa boş sonuç döndür
        if (!previousNodes.containsKey(target) && !start.equals(target)) {
            return new RouteResult(Collections.emptyList(), 0.0, 0.0, getStrategyDescription());
        }

        // Rotayı geriye doğru takip ederek oluştur
        List<Intersection> path = new ArrayList<>();
        Intersection step = target;
        while (step != null) {
            path.add(step);
            step = previousNodes.get(step);
        }
        Collections.reverse(path);

        return new RouteResult(path, actualDistances.get(target), minCosts.get(target), getStrategyDescription());
    }

    private double calculateRoadCost(Road road, Vehicle vehicle) {
        double distance = road.getDistanceKm();
        if (!considerTraffic) {
            return distance; // Sadece mesafe bazlı hesaplama
        }

        double congestionRatio = road.getCongestionRatio(); // 0.0 - 1.0 arası
        double penaltyMultiplier = vehicle.getTrafficPenaltyMultiplier(); // Normal: 2.5, Acil: 0.15

        // Yoğunluk karesiyle artan ceza puanı (Çok yoğun yollardan kaçınmayı sağlar)
        double trafficPenalty = 1.0 + (Math.pow(congestionRatio, 2) * 10.0 * penaltyMultiplier);
        return distance * trafficPenalty;
    }

    private String getStrategyDescription() {
        return considerTraffic ? "Akıllı Trafik ve Öncelik Algoritması (Dinamik Dijkstra)" : "Sadece En Kısa Mesafe (Standart Dijkstra)";
    }
}