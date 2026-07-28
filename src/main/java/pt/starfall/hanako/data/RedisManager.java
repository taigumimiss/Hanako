package pt.starfall.hanako.data;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import pt.starfall.hanako.config.ConfigManager;
import pt.starfall.hanako.util.SchedulerUtil;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class RedisManager {
    private static RedisManager instance;

    private RedisClient client;
    private io.lettuce.core.api.StatefulRedisConnection<String, String> connection;
    private RedisPubSubAsyncCommands<String, String> pubSub;
    private io.lettuce.core.pubsub.StatefulRedisPubSubConnection<String, String> pubSubConnection;

    private static final String VANISH_SET = "hanako:vanish";
    private static final String VANISH_CHANNEL = "hanako:vanish:sync";

    private final CopyOnWriteArrayList<Consumer<UUID>> vanishListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Consumer<UUID>> unvanishListeners = new CopyOnWriteArrayList<>();

    private RedisManager() {
    }

    public static void initialize() {
        if (instance != null) {
            throw new IllegalStateException("RedisManager already is initialized");
        }
        instance = new RedisManager();
    }

    public static RedisManager getInstance() {
        return instance;
    }

    public void connect() {
        ConfigManager config = ConfigManager.getInstance();
        if (!config.isRedisEnabled()) return;

        String uri;
        if (config.getRedisPassword().isEmpty()) {
            uri = "redis://" + config.getRedisHost() + ":" + config.getRedisPort();
        } else {
            uri = "redis://:" + config.getRedisPassword() + "@" + config.getRedisHost() + ":" + config.getRedisPort();
        }

        client = RedisClient.create(uri);
        connection = client.connect();
        pubSubConnection = client.connectPubSub();
        pubSub = pubSubConnection.async();

        pubSubConnection.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String channel, String message) {
                if (!VANISH_CHANNEL.equals(channel)) return;

                String[] parts = message.split(":");
                if (parts.length != 2) return;

                UUID uuid = UUID.fromString(parts[0]);
                boolean vanished = Boolean.parseBoolean(parts[1]);

                SchedulerUtil.runSync(() -> {
                    if (vanished) {
                        vanishListeners.forEach(listener -> listener.accept(uuid));
                    } else {
                        unvanishListeners.forEach(listener -> listener.accept(uuid));
                    }
                });
            }
        });

        pubSub.subscribe(VANISH_CHANNEL).toCompletableFuture().join();
    }

    public boolean isConnected() {
        return connection != null && connection.isOpen();
    }

    public void setVanished(UUID uuid, boolean vanished) {
        if (!isConnected()) return;

        if (vanished) {
            connection.async().sadd(VANISH_SET, uuid.toString());
        } else {
            connection.async().srem(VANISH_SET, uuid.toString());
        }

        pubSub.publish(VANISH_CHANNEL, uuid + ":" + vanished).toCompletableFuture().join();
    }

    public boolean isVanished(UUID uuid) {
        if (!isConnected()) return false;
        return connection.sync().sismember(VANISH_SET, uuid.toString());
    }

    public void addVanishListener(Consumer<UUID> listener) {
        vanishListeners.add(listener);
    }

    public void addUnvanishListener(Consumer<UUID> listener) {
        unvanishListeners.add(listener);
    }

    public void disconnect() {
        if (pubSubConnection != null) {
            pubSub.unsubscribe(VANISH_CHANNEL).toCompletableFuture().join();
            pubSubConnection.close();
        }
        if (connection != null) {
            connection.close();
        }
        if (client != null) {
            client.shutdown();
        }
    }
}
