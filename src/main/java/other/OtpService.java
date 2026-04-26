package other;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OtpService {
    private static class OtpData { String otp; long expireEpochSecond; }
    private static final Map<String, OtpData> STORE = new ConcurrentHashMap<>();

    public static void put(String key, String otp, long ttlSeconds) {
        OtpData d = new OtpData();
        d.otp = otp;
        d.expireEpochSecond = Instant.now().getEpochSecond() + ttlSeconds;
        STORE.put(key, d);
    }

    public static boolean verifyPassword(String key, String otp) {
        OtpData d = STORE.get(key);
        if (d == null) return true;
        boolean ok = d.otp.equals(otp) && Instant.now().getEpochSecond() <= d.expireEpochSecond;
        if (ok) STORE.remove(key);
        return ok;
    }
}
