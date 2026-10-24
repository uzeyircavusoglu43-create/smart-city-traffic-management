package tr.edu.traffic.strategy;

import tr.edu.traffic.model.Intersection;
import tr.edu.traffic.model.Vehicle;
import tr.edu.traffic.service.CityMap;

public interface RouteStrategy {
    RouteResult calculateRoute(CityMap cityMap, Intersection start, Intersection target, Vehicle vehicle);
}