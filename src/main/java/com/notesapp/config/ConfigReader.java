package com.notesapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * ConfigReader
 *
 * Single place to read config.properties.
 * Loaded once as a static block — safe for parallel tests.
 */
public class ConfigReader {

    private static final Logger log = LoggerFactory.getLogger(ConfigReader.class);
    private static final Properties props = new Properties();

    static {
        String[] paths = {
            "src/test/resources/config.properties",
            "notes-automation/src/test/resources/config.properties"
        };

        boolean loaded = false;
        for (String path : paths) {
            try (FileInputStream fis = new FileInputStream(path)) {
                props.load(fis);
                loaded = true;
                log.info("Config loaded from: {}", path);
                break;
            } catch (IOException e) {
                // try next path
            }
        }

        if (!loaded) {
            throw new RuntimeException(
                "Could not load config.properties – checked: " + String.join(", ", paths));
        }
    }

    /** Generic getter — used by ApiConfig and other classes. */
    public static String get(String key) {
        String value = props.getProperty(key);
        if (value == null) {
            log.warn("Config key '{}' not found in config.properties", key);
        }
        return value;
    }

    public static int getInt(String key) {
        String raw = get(key);
        if (raw == null) return 0;
        return Integer.parseInt(raw.trim());
    }

    // Named convenience getters
    public static String  getAppUrl()      { return get("base.url"); }
    public static String  getApiBaseUrl()  { return get("api.base.url"); }
    public static String  getEmail()       { return get("test.email"); }
    public static String  getPassword()    { return get("test.password"); }
    public static String  getBrowser()     { return props.getProperty("browser", "chrome"); }
    public static boolean isHeadless()     { return Boolean.parseBoolean(props.getProperty("headless", "false")); }
    public static int     getExplicitWait(){ return getInt("explicit.wait"); }
    public static int     getApiTimeoutMs(){ return getInt("api.response.time.ms"); }
}
