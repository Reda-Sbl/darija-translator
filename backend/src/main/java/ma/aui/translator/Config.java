package ma.aui.translator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Loads application.properties; environment variables override file values. */
public final class Config {
    private static final Properties P = new Properties();

    static {
        try (InputStream in = Config.class.getResourceAsStream("/application.properties")) {
            if (in != null) P.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Config() {}

    public static String get(String key) {
        String env = System.getenv(key.toUpperCase().replace('.', '_'));
        return (env != null && !env.isBlank()) ? env : P.getProperty(key, "");
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }
}
