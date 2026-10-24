package tr.edu.traffic.model;

public class Road {
    private final Intersection source;
    private final Intersection destination;
    private final double distanceKm;
    private final int maxCapacity;
    private int currentVehicleCount;

    public Road(Intersection source, Intersection destination, double distanceKm, int maxCapacity, int currentVehicleCount) {
        if (source == null || destination == null) {
            throw new IllegalArgumentException("Başlangıç ve bitiş kavşakları boş olamaz!");
        }
        if (distanceKm <= 0 || maxCapacity <= 0) {
            throw new IllegalArgumentException("Mesafe ve kapasite 0'dan büyük olmalıdır!");
        }
        this.source = source;
        this.destination = destination;
        this.distanceKm = distanceKm;
        this.maxCapacity = maxCapacity;
        setCurrentVehicleCount(currentVehicleCount); // Kapsülleme kontrolü
    }

    // Kapsülleme: Negatif araç sayısı girilemez, kapasite aşımı kontrol edilir
    public void setCurrentVehicleCount(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Araç sayısı negatif olamaz!");
        }
        if (count > maxCapacity) {
            throw new IllegalArgumentException("Araç sayısı yolun maksimum kapasitesini (" + maxCapacity + ") aşamaz!");
        }
        this.currentVehicleCount = count;
    }

    // Yoğunluk Oranı (0.0 ile 1.0 arası)
    public double getCongestionRatio() {
        return (double) currentVehicleCount / maxCapacity;
    }

    // Yoğunluk Seviyesi Metni
    public String getCongestionLevel() {
        double ratio = getCongestionRatio();
        if (ratio >= 0.80) return "KRİTİK (Çok Yoğun)";
        if (ratio >= 0.50) return "ORTA (Akıcı Yoğun)";
        return "DÜŞÜK (Açık)";
    }

    public Intersection getSource() { return source; }
    public Intersection getDestination() { return destination; }
    public double getDistanceKm() { return distanceKm; }
    public int getMaxCapacity() { return maxCapacity; }
    public int getCurrentVehicleCount() { return currentVehicleCount; }

    @Override
    public String toString() {
        return String.format("%s -> %s | %.1f km | Araç: %d/%d (%%%d - %s)",
                source.getName(), destination.getName(), distanceKm,
                currentVehicleCount, maxCapacity, (int)(getCongestionRatio() * 100), getCongestionLevel());
    }
}