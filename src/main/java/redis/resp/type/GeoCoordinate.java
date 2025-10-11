package redis.resp.type;

public class GeoCoordinate {

    private final double longitude;
    private final double latitude;

    private final double MIN_LATITUDE = -85.05112878;
    private final double MAX_LATITUDE = 85.05112878;
    private final double MIN_LONGITUDE = -180.0;
    private final double MAX_LONGITUDE = 180.0;

    private final double LATITUDE_RANGE = MAX_LATITUDE - MIN_LATITUDE;
    private final double LONGITUDE_RANGE = MAX_LONGITUDE - MIN_LONGITUDE;

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

    public boolean isValid() {
        return (
            longitude >= MIN_LONGITUDE &&
            longitude <= MAX_LONGITUDE &&
            latitude >= MIN_LATITUDE &&
            latitude <= MAX_LATITUDE
        );
    }
}
