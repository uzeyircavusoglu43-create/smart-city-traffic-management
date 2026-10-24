package tr.edu.traffic;

import tr.edu.traffic.model.*;
import tr.edu.traffic.repository.FileRepository;
import tr.edu.traffic.service.CityMap;
import tr.edu.traffic.service.EmergencyDispatchService;
import tr.edu.traffic.service.TrafficAnalyzerService;
import tr.edu.traffic.strategy.RouteResult;
import tr.edu.traffic.strategy.RouteStrategy;
import tr.edu.traffic.strategy.SmartTrafficRouteStrategy;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    private static final String DATA_FILE = "data/traffic_data.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        FileRepository repository = new FileRepository(DATA_FILE);
        CityMap cityMap = repository.initializeDefaultCityIfEmpty();

        TrafficAnalyzerService analyzerService = new TrafficAnalyzerService();
        RouteStrategy smartStrategy = new SmartTrafficRouteStrategy(true);
        RouteStrategy shortestStrategy = new SmartTrafficRouteStrategy(false);
        EmergencyDispatchService emergencyService = new EmergencyDispatchService(smartStrategy);

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> displayCityStatus(cityMap);
                    case "2" -> System.out.println(analyzerService.generateCongestionReport(cityMap));
                    case "3" -> updateRoadTraffic(scanner, cityMap, repository);
                    case "4" -> calculateNormalRoute(scanner, cityMap, smartStrategy, shortestStrategy);
                    case "5" -> dispatchEmergency(scanner, cityMap, emergencyService, repository);
                    case "6" -> addNewIntersectionOrRoad(scanner, cityMap, repository);
                    case "0" -> {
                        repository.saveCityMap(cityMap);
                        System.out.println("[BAŞARILI] Tüm veriler '" + DATA_FILE + "' dosyasına kaydedildi. Çıkış yapılıyor...");
                        running = false;
                    }
                    default -> System.out.println("[UYARI] Geçersiz seçim! Lütfen 0-6 arasında bir değer girin.");
                }
            } catch (Exception e) {
                System.out.println("[HATA] İşlem sırasında hata oluştu: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n=======================================================");
        System.out.println("   AKILLI ŞEHİR TRAFİK YÖNETİM VE ROTA SİSTEMİ (v1.0)");
        System.out.println("=======================================================");
        System.out.println("1. Şehir Haritasını, Kavşakları ve Yolları Görüntüle");
        System.out.println("2. Trafik Yoğunluğu Analiz Raporu Al");
        System.out.println("3. Yoldaki Araç Sayısını Güncelle (Trafik Simülasyonu)");
        System.out.println("4. Normal Araç İçin En Uygun Rota Hesapla (Karşılaştırmalı)");
        System.out.println("5. ACİL DURUM ARACI (Ambulans/İtfaiye) Sevk Et!");
        System.out.println("6. Yeni Kavşak veya Yeni Yol Ekle");
        System.out.println("0. Verileri Kaydet ve Çıkış Yap");
        System.out.println("-------------------------------------------------------");
        System.out.print("Seçiminiz: ");
    }

    private static void displayCityStatus(CityMap cityMap) {
        System.out.println("\n--- KAVŞAKLAR VE TRAFİK IŞIĞI DURUMLARI ---");
        for (Intersection i : cityMap.getAllIntersections()) {
            System.out.println(" * " + i);
        }
        System.out.println("\n--- YOLLAR VE ANLIK YOĞUNLUK DURUMLARI ---");
        for (Road r : cityMap.getAllRoads()) {
            System.out.println(" -> " + r);
        }
    }

    private static void updateRoadTraffic(Scanner scanner, CityMap cityMap, FileRepository repo) throws IOException {
        System.out.print("Başlangıç Kavşak ID (Örn: K1): ");
        String srcId = scanner.nextLine().trim().toUpperCase();
        System.out.print("Bitiş Kavşak ID (Örn: K4): ");
        String dstId = scanner.nextLine().trim().toUpperCase();

        Road road = cityMap.findRoad(srcId, dstId);
        if (road == null) {
            System.out.println("[HATA] Belirtilen kavşaklar arasında doğrudan bir yol bulunamadı!");
            return;
        }

        System.out.println("Mevcut Durum: " + road);
        System.out.print("Yeni Araç Sayısını Girin (Maks " + road.getMaxCapacity() + "): ");
        int newCount = Integer.parseInt(scanner.nextLine().trim());

        road.setCurrentVehicleCount(newCount);
        repo.saveCityMap(cityMap);
        System.out.println("[GÜNCELLENDİ] Yeni Yol Durumu: " + road);
    }

    private static void calculateNormalRoute(Scanner scanner, CityMap cityMap, RouteStrategy smartStrategy, RouteStrategy shortestStrategy) {
        System.out.print("Araç Plakası Girin (Örn: 34ABC123): ");
        String plate = scanner.nextLine().trim();
        Vehicle normalCar = new StandardVehicle(plate, "Binek Araç", 90.0);

        System.out.print("Başlangıç Kavşak ID (Örn: K1): ");
        Intersection start = cityMap.getIntersectionById(scanner.nextLine().trim().toUpperCase());
        System.out.print("Hedef Kavşak ID (Örn: K5): ");
        Intersection target = cityMap.getIntersectionById(scanner.nextLine().trim().toUpperCase());

        if (start == null || target == null) {
            System.out.println("[HATA] Geçersiz kavşak ID'si girdiniz!");
            return;
        }

        System.out.println("\nAraç Bilgisi: " + normalCar);
        RouteResult shortestResult = shortestStrategy.calculateRoute(cityMap, start, target, normalCar);
        System.out.println(shortestResult);

        RouteResult smartResult = smartStrategy.calculateRoute(cityMap, start, target, normalCar);
        System.out.println(smartResult);
    }

    private static void dispatchEmergency(Scanner scanner, CityMap cityMap, EmergencyDispatchService emergencyService, FileRepository repo) throws IOException {
        System.out.print("Acil Araç Plakası (Örn: 34ACIL112): ");
        String plate = scanner.nextLine().trim();
        System.out.print("Acil Araç Tipi (Ambulans / İtfaiye / Polis): ");
        String type = scanner.nextLine().trim();

        Vehicle emergencyVehicle = new EmergencyVehicle(plate, type, 120.0);

        System.out.print("Başlangıç Kavşak ID (Örn: K1): ");
        Intersection start = cityMap.getIntersectionById(scanner.nextLine().trim().toUpperCase());
        System.out.print("Hedef Kavşak ID (Örn: K5): ");
        Intersection target = cityMap.getIntersectionById(scanner.nextLine().trim().toUpperCase());

        if (start == null || target == null) {
            System.out.println("[HATA] Geçersiz kavşak ID'si girdiniz!");
            return;
        }

        RouteResult result = emergencyService.dispatchEmergencyVehicle(cityMap, start, target, emergencyVehicle);
        repo.saveCityMap(cityMap);
        System.out.println("\n" + result);
    }

    private static void addNewIntersectionOrRoad(Scanner scanner, CityMap cityMap, FileRepository repo) throws IOException {
        System.out.println("1 - Yeni Kavşak Ekle | 2 - Yeni Yol Ekle");
        System.out.print("Seçim: ");
        String subChoice = scanner.nextLine().trim();

        if ("1".equals(subChoice)) {
            System.out.print("Yeni Kavşak ID (Örn: K6): ");
            String id = scanner.nextLine().trim().toUpperCase();
            System.out.print("Kavşak Adı (Örn: Havaalanı Kavşağı): ");
            String name = scanner.nextLine().trim();

            cityMap.addIntersection(new Intersection(id, name));
            repo.saveCityMap(cityMap);
            System.out.println("[BAŞARILI] Kavşak eklendi ve kaydedildi.");
        } else if ("2".equals(subChoice)) {
            System.out.print("Başlangıç Kavşak ID: ");
            Intersection src = cityMap.getIntersectionById(scanner.nextLine().trim().toUpperCase());
            System.out.print("Bitiş Kavşak ID: ");
            Intersection dst = cityMap.getIntersectionById(scanner.nextLine().trim().toUpperCase());

            if (src == null || dst == null) {
                System.out.println("[HATA] Önce kavşakların sistemde tanımlı olması gerekir!");
                return;
            }
            System.out.print("Mesafe (km, Örn: 5.5): ");
            double dist = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Maksimum Araç Kapasitesi (Örn: 120): ");
            int cap = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Anlık Araç Sayısı (Örn: 30): ");
            int cur = Integer.parseInt(scanner.nextLine().trim());

            cityMap.addBidirectionalRoad(src, dst, dist, cap, cur);
            repo.saveCityMap(cityMap);
            System.out.println("[BAŞARILI] Çift yönlü yol eklendi ve kaydedildi.");
        }
    }
}