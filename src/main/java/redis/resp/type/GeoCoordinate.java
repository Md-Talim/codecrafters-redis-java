package redis.resp.type;

public class GeoCoordinate {

    private final double longitude;
    private final double latitude;

    private static final double MIN_LATITUDE = -85.05112878;
    private static final double MAX_LATITUDE = 85.05112878;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;

    private static final double LATITUDE_RANGE = MAX_LATITUDE - MIN_LATITUDE;
    private static final double LONGITUDE_RANGE = MAX_LONGITUDE - MIN_LONGITUDE;

    public GeoCoordinate(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    // https://github.com/codecrafters-io/redis-geocoding-algorithm/blob/main/java/Encode.java
    public long encode() {
        double normalizedLatitude =
            (Math.pow(2, 26) * (latitude - MIN_LATITUDE)) / LATITUDE_RANGE;
        double normalizedLongitude =
            (Math.pow(2, 26) * (longitude - MIN_LONGITUDE)) / LONGITUDE_RANGE;

        int truncatedLatitude = (int) normalizedLatitude;
        int truncatedLongitude = (int) normalizedLongitude;

        return interleave(truncatedLatitude, truncatedLongitude);
    }

    private long interleave(int x, int y) {
        long spreadX = spreadInt32ToInt64(x);
        long spreadY = spreadInt32ToInt64(y);
        long shiftedY = spreadY << 1;
        return spreadX | shiftedY;
    }

    private long spreadInt32ToInt64(int v) {
        long result = v & 0xFFFFFFFFL;
        result = (result | (result << 16)) & 0x0000FFFF0000FFFFL;
        result = (result | (result << 8)) & 0x00FF00FF00FF00FFL;
        result = (result | (result << 4)) & 0x0F0F0F0F0F0F0F0FL;
        result = (result | (result << 2)) & 0x3333333333333333L;
        result = (result | (result << 1)) & 0x5555555555555555L;
        return result;
    }

    public static Coordinate decode(long geoHash) {
        long y = geoHash >> 1;
        long x = geoHash;

        int gridLatitude = compactInt64ToInt32(x);
        int gridLongitude = compactInt64ToInt32(y);

        return convertGridNumbersToCoordinate(gridLatitude, gridLongitude);
    }

    private static int compactInt64ToInt32(long v) {
        v = v & 0x5555555555555555L;
        v = (v | (v >> 1)) & 0x3333333333333333L;
        v = (v | (v >> 2)) & 0x0F0F0F0F0F0F0F0FL;
        v = (v | (v >> 4)) & 0x00FF00FF00FF00FFL;
        v = (v | (v >> 8)) & 0x0000FFFF0000FFFFL;
        v = (v | (v >> 16)) & 0x00000000FFFFFFFFL;
        return (int) v;
    }

    private static Coordinate convertGridNumbersToCoordinate(
        int gridLatitude,
        int gridLongitude
    ) {
        double gridLatitudeMin =
            MIN_LATITUDE + LATITUDE_RANGE * (gridLatitude / Math.pow(2, 26));
        double gridLatitudeMax =
            MIN_LATITUDE +
            LATITUDE_RANGE * ((gridLatitude + 1) / Math.pow(2, 26));
        double gridLongitudeMin =
            MIN_LONGITUDE + LONGITUDE_RANGE * (gridLongitude / Math.pow(2, 26));
        double gridLongitudeMax =
            MIN_LONGITUDE +
            LONGITUDE_RANGE * ((gridLongitude + 1) / Math.pow(2, 26));

        double latitude = (gridLatitudeMin + gridLatitudeMax) / 2;
        double longitude = (gridLongitudeMin + gridLongitudeMax) / 2;

        return new Coordinate(latitude, longitude);
    }

    public boolean isValid() {
        return (
            longitude >= MIN_LONGITUDE &&
            longitude <= MAX_LONGITUDE &&
            latitude >= MIN_LATITUDE &&
            latitude <= MAX_LATITUDE
        );
    }

    public record Coordinate(double latitude, double longitude) {}
}
