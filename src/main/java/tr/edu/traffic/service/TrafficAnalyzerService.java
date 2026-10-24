package tr.edu.traffic.service;

import tr.edu.traffic.model.Road;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TrafficAnalyzerService {

    // Yoğunluğu %80 ve üzeri olan KRİTİK yolları döndürür
    public List<Road> getCriticalRoads(CityMap cityMap) {
        return cityMap.getAllRoads().stream()
                .filter(road -> road.getCongestionRatio() >= 0.80)
                .sorted(Comparator.comparingDouble(Road::getCongestionRatio).reversed())
                .collect(Collectors.toList());
    }

    // Şehir genelindeki ortalama trafik yoğunluk yüzdesini hesaplar
    public double calculateAverageCityCongestion(CityMap cityMap) {
        List<Road> roads = cityMap.getAllRoads();
        if (roads.isEmpty()) return 0.0;

        double totalRatio = 0.0;
        for (Road road : roads) {
            totalRatio += road.getCongestionRatio();
        }
        return (totalRatio / roads.size()) * 100.0;
    }

    // Konsol için detaylı trafik analiz raporu üretir
    public String generateCongestionReport(CityMap cityMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n================ ŞEHİR TRAFİK YOĞUNLUK RAPORU ================\n");
        sb.append(String.format("Şehir Genel Yoğunluk Ortalaması: %%.1f\n", calculateAverageCityCongestion(cityMap)));

        List<Road> criticalRoads = getCriticalRoads(cityMap);
        sb.append("Kritik Durumdaki Yol Sayısı (%%80+): ").append(criticalRoads.size()).append("\n");
        sb.append("--------------------------------------------------------------\n");

        if (criticalRoads.isEmpty()) {
            sb.append("Şu an şehirde kritik yoğunlukta (darboğaz) bir yol bulunmamaktadır.\n");
        } else {
            sb.append("ACİL MÜDAHALE GEREKTİREN KRİTİK YOLLAR:\n");
            for (Road road : criticalRoads) {
                sb.append(" [!] ").append(road.toString()).append("\n");
            }
        }
        sb.append("==============================================================\n");
        return sb.toString();
    }
}