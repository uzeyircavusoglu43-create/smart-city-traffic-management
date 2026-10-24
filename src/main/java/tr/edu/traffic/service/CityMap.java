package tr.edu.traffic.service;

import tr.edu.traffic.model.Intersection;
import tr.edu.traffic.model.Road;

import java.util.*;

public class CityMap {
    private final Map<String, Intersection> intersections;
    private final Map<Intersection, List<Road>> adjacencyList;

    public CityMap() {
        this.intersections = new HashMap<>();
        this.adjacencyList = new HashMap<>();
    }

    // Şehre yeni bir kavşak ekler
    public void addIntersection(Intersection intersection) {
        if (intersection == null) {
            throw new IllegalArgumentException("Kavşak boş olamaz!");
        }
        intersections.putIfAbsent(intersection.getId(), intersection);
        adjacencyList.putIfAbsent(intersection, new ArrayList<>());
    }

    // İki kavşak arasına yol ekler
    public void addRoad(Road road) {
        if (road == null) {
            throw new IllegalArgumentException("Yol boş olamaz!");
        }
        addIntersection(road.getSource());
        addIntersection(road.getDestination());
        adjacencyList.get(road.getSource()).add(road);
    }

    // Çift yönlü yol ekleme kolaylığı sağlayan metot (Overloading)
    public void addBidirectionalRoad(Intersection a, Intersection b, double distanceKm, int maxCapacity, int currentVehicles) {
        addRoad(new Road(a, b, distanceKm, maxCapacity, currentVehicles));
        addRoad(new Road(b, a, distanceKm, maxCapacity, currentVehicles));
    }

    public Intersection getIntersectionById(String id) {
        return intersections.get(id);
    }

    public Collection<Intersection> getAllIntersections() {
        return Collections.unmodifiableCollection(intersections.values());
    }

    public List<Road> getRoadsFrom(Intersection intersection) {
        return adjacencyList.getOrDefault(intersection, Collections.emptyList());
    }

    public List<Road> getAllRoads() {
        List<Road> allRoads = new ArrayList<>();
        for (List<Road> roads : adjacencyList.values()) {
            allRoads.addAll(roads);
        }
        return allRoads;
    }

    // İki kavşak arasındaki yolu bulur.
    public Road findRoad(String sourceId, String destId) {
        Intersection src = intersections.get(sourceId);
        if (src == null) return null;
        for (Road road : getRoadsFrom(src)) {
            if (road.getDestination().getId().equalsIgnoreCase(destId)) {
                return road;
            }
        }
        return null;
    }
}