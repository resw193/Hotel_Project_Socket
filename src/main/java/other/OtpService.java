package other;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OtpService {

    private static class OtpData {
        String otp;
        long expireEpochSecond;
    }

    private static final Map<String, OtpData> STORE = new ConcurrentHashMap<>();

    public static void put(String key, String otp, long ttlSeconds) {
        OtpData d = new OtpData();
        d.otp = otp;
        d.expireEpochSecond = Instant.now().getEpochSecond() + ttlSeconds;
        STORE.put(normalize(key), d);
    }

    public static boolean verifyPassword(String key, String otp) {
        key = normalize(key);
        otp = otp == null ? "" : otp.trim();

        OtpData d = STORE.get(key);
        if (d == null) {
            return false;
        }

        boolean notExpired = Instant.now().getEpochSecond() <= d.expireEpochSecond;
        boolean ok = d.otp.equals(otp) && notExpired;

        if (ok || !notExpired) {
            STORE.remove(key);
        }

        return ok;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}