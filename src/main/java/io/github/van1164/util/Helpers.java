package io.github.van1164.util;

public class Helpers {

    public static double toMillis(String token) {
        if (token.endsWith("ms"))  {           // 200ms
            return Double.parseDouble(token.replace("ms", ""));
        }
        if (token.endsWith("µs") || token.endsWith("us")) {  // 688µs or 688us
            return Double.parseDouble(token.replace("µs", "").replace("us", "")) / 1_000;
        }
        if (token.endsWith("ns")) {            // nanoseconds (rare)
            return Double.parseDouble(token.replace("ns", "")) / 1_000_000;
        }
        if (token.endsWith("s")) {             // 1.23s
            return Double.parseDouble(token.replace("s", "")) * 1_000;
        }

        // fallback: assume already ms
        return Double.parseDouble(token);
    }
    public static long toBytes(String value, String unit) {
        double num = Double.parseDouble(value);
        return switch (unit) {
            case "kB" -> (long)(num * 1024);
            case "MB" -> (long)(num * 1024 * 1024);
            case "B"  -> (long) num;
            default   -> (long) num;
        };
    }
}
