package other;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ReceiptCache {
    private static final Map<String, Double> CASH = new ConcurrentHashMap<>();
    private static final Path STORE = Paths.get(System.getProperty("user.home"),
            ".mimosa", "receipt-cache.properties");

    static {
        try {
            if (Files.exists(STORE)) {
                Properties p = new Properties();
                try (InputStream in = Files.newInputStream(STORE)) {
                    p.load(in);
                }
                for (String k : p.stringPropertyNames()) {
                    CASH.put(k, Double.parseDouble(p.getProperty(k, "0")));
                }
            }
        } catch (Exception ignored) {

        }
    }

    private static String key(String orderId) {
        return orderId == null ? "" : orderId.trim().toUpperCase(Locale.ROOT);
    }

    public static void setCashReceived(String orderId, double cash) {
        String k = key(orderId);
        if (k.isEmpty()) return;
        CASH.put(k, cash);

        persist();
    }

    public static Double getCashReceived(String orderId) {
        return CASH.get(key(orderId));
    }

    private static synchronized void persist() {
        try {
            Files.createDirectories(STORE.getParent());
            Properties p = new Properties();
            for (var e : CASH.entrySet()) p.setProperty(e.getKey(), Double.toString(e.getValue()));
            try (OutputStream out = Files.newOutputStream(STORE,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                p.store(out, "receipt cash cache");
            }
        } catch (Exception ignored) {}
    }
}
