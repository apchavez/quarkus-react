package com.products.health;

import com.mongodb.client.MongoClient;
import io.vertx.mutiny.redis.client.Redis;
import io.vertx.mutiny.redis.client.Request;
import io.vertx.mutiny.redis.client.Command;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import java.time.Duration;

@Readiness
@ApplicationScoped
public class ReadinessCheck implements HealthCheck {

    @ConfigProperty(name = "app.name", defaultValue = "products-api")
    String appName;

    @ConfigProperty(name = "app.environment", defaultValue = "local")
    String environment;

    @Inject
    MongoClient mongoClient;

    @Inject
    Redis redis;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named(appName + "-readiness")
                .withData("environment", environment);

        boolean mongoOk = checkMongo(builder);
        boolean redisOk = checkRedis(builder);

        return (mongoOk && redisOk) ? builder.up().build() : builder.down().build();
    }

    private boolean checkMongo(HealthCheckResponseBuilder builder) {
        try {
            mongoClient.getDatabase("admin").runCommand(new Document("ping", 1));
            builder.withData("mongodb", "up");
            return true;
        } catch (Exception e) {
            builder.withData("mongodb", "down").withData("mongodb-error", e.getMessage());
            return false;
        }
    }

    private boolean checkRedis(HealthCheckResponseBuilder builder) {
        try {
            redis.send(Request.cmd(Command.PING)).await().atMost(Duration.ofSeconds(2));
            builder.withData("redis", "up");
            return true;
        } catch (Exception e) {
            builder.withData("redis", "down").withData("redis-error", e.getMessage());
            return false;
        }
    }
}
