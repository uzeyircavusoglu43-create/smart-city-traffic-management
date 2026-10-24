package tr.edu.traffic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tr.edu.traffic.model.*;
import tr.edu.traffic.repository.FileRepository;
import tr.edu.traffic.service.CityMap;
import tr.edu.traffic.service.EmergencyDispatchService;
import tr.edu.traffic.service.TrafficAnalyzerService;
import tr.edu.traffic.strategy.RouteResult;
import tr.edu.traffic.strategy.RouteStrategy;
import tr.edu.traffic.strategy.SmartTrafficRouteStrategy;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TrafficSystemTest {

    private CityMap cityMap;
    private Intersection k1, k2, k3, k4;

    @BeforeEach
    void setUp() {
        cityMap = new CityMap();
        k1 = new Intersection("K1", "Merkez");
        k2 = new Intersection("K2", "Kuzey Yolu");
        k3 = new Intersection("K3", "Güney Yolu");
        k4 = new Intersection("K4", "Hastane");

        cityMap.addIntersection(k1);
        cityMap.addIntersection(k2);
        cityMap.addIntersection(k3);
        cityMap.addIntersection(k4);
    }

    // TEST 1: Yoğunluk Oranı Hesaplama Testi
    @Test
    @DisplayName("1. Yol yoğunluk oranı ve kritik seviye doğru hesaplanmalı")
    void testRoadCongestionCalculation() {
        Road road = new Road(k1, k2, 5.0, 100, 85);
        assertEquals(0.85, road.getCongestionRatio(), 0.001);
        assertTrue(road.getCongestionLevel().contains("KRİTİK"));
    }

    // TEST 2: Kapsülleme ve Kapasite Aşımı Kontrolü
    @Test
    @DisplayName("2. Yol kapasitesinden fazla araç eklendiğinde hata fırlatılmalı (Kapsülleme)")
    void testRoadCapacityEncapsulation() {
        Road road = new Road(k1, k2, 5.0, 50, 20);
        assertThrows(IllegalArgumentException.class, () -> road.setCurrentVehicleCount(60));
        assertThrows(IllegalArgumentException.class, () -> road.setCurrentVehicleCount(-5));
    }

    // TEST 3: Çok Biçimlilik (Polymorphism) Öncelik Katsayısı Testi
    @Test
    @DisplayName("3. Acil durum aracının trafik ceza katsayısı normal araçtan düşük olmalı")
    void testVehiclePolymorphismPriority() {
        Vehicle normalCar = new StandardVehicle("34ABC12", "Otomobil", 80);
        Vehicle ambulance = new EmergencyVehicle("34ACIL112", "Ambulans", 110);

        assertFalse(normalCar.hasEmergencyPriority());
        assertTrue(ambulance.hasEmergencyPriority());
        assertTrue(ambulance.getTrafficPenaltyMultiplier() < normalCar.getTrafficPenaltyMultiplier());
    }

    // TEST 4: Standart En Kısa Mesafe Rota Testi
    @Test
    @DisplayName("4. Trafik yoksayıldığında fiziksel olarak en kısa mesafe seçilmeli")
    void testShortestDistanceRoute() {
        // K1 -> K2 -> K4 (Toplam 6 km)
        cityMap.addBidirectionalRoad(k1, k2, 3.0, 100, 90);
        cityMap.addBidirectionalRoad(k2, k4, 3.0, 100, 90);
        // K1 -> K3 -> K4 (Toplam 10 km)
        cityMap.addBidirectionalRoad(k1, k3, 5.0, 100, 10);
        cityMap.addBidirectionalRoad(k3, k4, 5.0, 100, 10);

        RouteStrategy shortestStrategy = new SmartTrafficRouteStrategy(false);
        Vehicle car = new StandardVehicle("34TEST01", "Otomobil", 80);
        RouteResult result = shortestStrategy.calculateRoute(cityMap, k1, k4, car);

        assertTrue(result.isRouteFound());
        assertEquals(6.0, result.getTotalDistanceKm(), 0.01);
        assertEquals(List.of(k1, k2, k4), result.getPath());
    }

    // TEST 5: Akıllı Algoritmanın Yoğun Trafikten Kaçınma Testi
    @Test
    @DisplayName("5. Normal araç yoğun olan kısa yol yerine açık olan alternatif yolu seçmeli")
    void testSmartRouteAvoidsCongestionForStandardVehicle() {
        // K1 -> K2 -> K4 (6 km ama %95 dolu - Çok Yoğun)
        cityMap.addBidirectionalRoad(k1, k2, 3.0, 100, 95);
        cityMap.addBidirectionalRoad(k2, k4, 3.0, 100, 95);
        // K1 -> K3 -> K4 (8 km ama %10 dolu - Açık)
        cityMap.addBidirectionalRoad(k1, k3, 4.0, 100, 10);
        cityMap.addBidirectionalRoad(k3, k4, 4.0, 100, 10);

        RouteStrategy smartStrategy = new SmartTrafficRouteStrategy(true);
        Vehicle normalCar = new StandardVehicle("34NORM01", "Otomobil", 80);
        RouteResult result = smartStrategy.calculateRoute(cityMap, k1, k4, normalCar);

        // Normal araç yoğun K2 yerine açık olan K3 üzerinden gitmeli
        assertEquals(List.of(k1, k3, k4), result.getPath());
    }

    // TEST 6: Acil Durum Aracının Geçiş Üstünlüğü ve Observer Yeşil Dalga Testi
    @Test
    @DisplayName("6. Acil araç sevk edildiğinde güzergahtaki kavşaklar EMERGENCY_GREEN olmalı")
    void testEmergencyDispatchAndObserverGreenWave() {
        cityMap.addBidirectionalRoad(k1, k2, 3.0, 100, 90);
        cityMap.addBidirectionalRoad(k2, k4, 3.0, 100, 90);

        EmergencyDispatchService dispatchService = new EmergencyDispatchService(new SmartTrafficRouteStrategy(true));
        Vehicle ambulance = new EmergencyVehicle("34AMB112", "Ambulans", 120);

        RouteResult result = dispatchService.dispatchEmergencyVehicle(cityMap, k1, k4, ambulance);

        assertTrue(result.isRouteFound());
        assertEquals(TrafficLightState.EMERGENCY_GREEN, k1.getLightState());
        assertEquals(TrafficLightState.EMERGENCY_GREEN, k2.getLightState());
        assertEquals(TrafficLightState.EMERGENCY_GREEN, k4.getLightState());
    }

    // TEST 7: Trafik Yoğunluğu Analiz Servisi Testi
    @Test
    @DisplayName("7. Analiz servisi kritik yoğunluktaki (%80+) yolları doğru tespit etmeli")
    void testTrafficAnalyzerCriticalRoads() {
        cityMap.addRoad(new Road(k1, k2, 4.0, 100, 85)); // Kritik (%85)
        cityMap.addRoad(new Road(k2, k3, 4.0, 100, 30)); // Normal (%30)

        TrafficAnalyzerService analyzer = new TrafficAnalyzerService();
        List<Road> criticalRoads = analyzer.getCriticalRoads(cityMap);

        assertEquals(1, criticalRoads.size());
        assertEquals(k1, criticalRoads.get(0).getSource());
    }

    // TEST 8: Kalıcı Veri (Dosya Kaydetme ve Okuma) Testi
    @Test
    @DisplayName("8. Şehir haritası dosyaya kaydedilip tekrar okunduğunda veriler korunmalı")
    void testFileRepositorySaveAndLoad() throws IOException {
        String testFile = "test_traffic_data.txt";
        FileRepository repo = new FileRepository(testFile);

        cityMap.addRoad(new Road(k1, k2, 4.5, 100, 65));
        repo.saveCityMap(cityMap);

        CityMap loadedMap = repo.loadCityMap();
        assertEquals(4, loadedMap.getAllIntersections().size());

        Road loadedRoad = loadedMap.findRoad("K1", "K2");
        assertNotNull(loadedRoad);
        assertEquals(65, loadedRoad.getCurrentVehicleCount());

        // Test dosyasını temizle
        new File(testFile).delete();
    }
}