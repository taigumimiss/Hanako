package pt.starfall.hanako.config;

import org.bukkit.configuration.file.FileConfiguration;
import pt.starfall.hanako.Hanako;

public class ConfigManager {
    private static ConfigManager instance;

    private boolean redisEnabled;
    private String redisHost;
    private int redisPort;
    private String redisPassword;
    private int redisDatabase;

    private ConfigManager() {
        load();
    }

    public static void initialize() {
        if (instance != null) {
            throw new IllegalStateException("ConfigManager already is initialized");
        }
        instance = new ConfigManager();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ConfigManager isnt initialized");
        }
        return instance;
    }

    public void load() {
        Hanako plugin = Hanako.getInstance();
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();
        redisEnabled = config.getBoolean("redis.enabled", false);
        redisHost = config.getString("redis.host", "localhost");
        redisPort = config.getInt("redis.port", 6379);
        redisPassword = config.getString("redis.password", "");
        redisDatabase = config.getInt("redis.database", 0);
    }

    public boolean isRedisEnabled() {
        return redisEnabled;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public int getRedisDatabase() {
        return redisDatabase;
    }
}
