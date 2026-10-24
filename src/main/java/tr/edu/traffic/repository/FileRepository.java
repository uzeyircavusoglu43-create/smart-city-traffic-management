package tr.edu.traffic.repository;

import tr.edu.traffic.model.Intersection;
import tr.edu.traffic.model.Road;
import tr.edu.traffic.model.TrafficLightState;
import tr.edu.traffic.service.CityMap;

import java.io.*;

public class FileRepository {
    private final String filePath;

    public FileRepository(String filePath) {
        this.filePath = filePath;
    }

    // Şehir haritasını (kavşakları ve yolları) dosyaya kalıcı olarak kaydeder
    public void saveCityMap(CityMap cityMap) throws IOException {
        File file = new File(filePath);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs(); // Eğer "data" klasörü yoksa otomatik oluşturur!
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // 1. Önce Kavşakları Kaydet
            for (Intersection i : cityMap.getAllIntersections()) {
                writer.write(String.format("INTERSECTION;%s;%s;%s\n",
                        i.getId(), i.getName(), i.getLightState().name()));
            }
            // 2. Sonra Yolları Kaydet
            for (Road r : cityMap.getAllRoads()) {
                writer.write(String.format("ROAD;%s;%s;%s;%d;%d\n",
                        r.getSource().getId(),
                        r.getDestination().getId(),
                        String.valueOf(r.getDistanceKm()),
                        r.getMaxCapacity(),
                        r.getCurrentVehicleCount()));
            }
        }
    }

    // Dosyadan verileri okuyup CityMap nesnesini geri yükler
    public CityMap loadCityMap() throws IOException {
        File file = new File(filePath);
        CityMap cityMap = new CityMap();

        if (!file.exists()) {
            return cityMap; // Dosya henüz yoksa boş harita döndür
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split(";");

                if ("INTERSECTION".equals(parts[0]) && parts.length >= 4) {
                    Intersection intersection = new Intersection(parts[1], parts[2]);
                    intersection.setLightState(TrafficLightState.valueOf(parts[3]));
                    cityMap.addIntersection(intersection);
                } else if ("ROAD".equals(parts[0]) && parts.length >= 6) {
                    Intersection src = cityMap.getIntersectionById(parts[1]);
                    Intersection dst = cityMap.getIntersectionById(parts[2]);
                    double distance = Double.parseDouble(parts[3]);
                    int capacity = Integer.parseInt(parts[4]);
                    int currentVehicles = Integer.parseInt(parts[5]);

                    if (src != null && dst != null) {
                        cityMap.addRoad(new Road(src, dst, distance, capacity, currentVehicles));
                    }
                }
            }
        }
        return cityMap;
    }

    // İlk çalıştırmada dosya yoksa varsayılan örnek bir şehir haritası yükler
    public CityMap initializeDefaultCityIfEmpty() {
        try {
            CityMap loadedMap = loadCityMap();
            if (!loadedMap.getAllIntersections().isEmpty()) {
                System.out.println("[BİLGİ] Kayıtlı şehir verileri '" + filePath + "' dosyasından yüklendi.");
                return loadedMap;
            }
        } catch (IOException e) {
            System.out.println("[UYARI] Dosya okunamadı, varsayılan harita oluşturuluyor...");
        }

        CityMap defaultMap = new CityMap();
        Intersection k1 = new Intersection("K1", "Merkez Meydan");
        Intersection k2 = new Intersection("K2", "Üniversite Kavşağı");
        Intersection k3 = new Intersection("K3", "Sanayi Bulvarı");
        Intersection k4 = new Intersection("K4", "Köprü Girişi");
        Intersection k5 = new Intersection("K5", "Şehir Hastanesi");

        // Çift yönlü yollar ekleniyor (Kavşak1, Kavşak2, MesafeKm, Kapasite, AnlıkAraç)
        // K1 -> K4 -> K5 kısa yol ama K1-K4 arası ÇOK YOĞUN (95/100)
        defaultMap.addBidirectionalRoad(k1, k4, 4.0, 100, 95);
        defaultMap.addBidirectionalRoad(k4, k5, 3.0, 100, 85);

        // K1 -> K2 -> K3 -> K5 biraz daha uzun yol ama TRAFİK AÇIK (20/100)
        defaultMap.addBidirectionalRoad(k1, k2, 3.0, 100, 20);
        defaultMap.addBidirectionalRoad(k2, k3, 3.5, 100, 15);
        defaultMap.addBidirectionalRoad(k3, k5, 3.0, 100, 25);

        // Ara bağlantı
        defaultMap.addBidirectionalRoad(k2, k4, 2.5, 80, 40);

        try {
            saveCityMap(defaultMap);
            System.out.println("[BİLGİ] Varsayılan şehir haritası oluşturuldu ve '" + filePath + "' dosyasına kaydedildi.");
        } catch (IOException e) {
            System.out.println("[HATA] Varsayılan harita kaydedilemedi: " + e.getMessage());
        }

        return defaultMap;
    }
}