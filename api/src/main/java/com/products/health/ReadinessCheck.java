package com.products.health;

import com.mongodb.client.MongoClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class ReadinessCheck implements HealthCheck {

    @ConfigProperty(name = "app.name", defaultValue = "products-api")
    String appName;

    @ConfigProperty(name = "app.environment", defaultValue = "local")
    String environment;

    @Inject
    MongoClient mongoClient;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named(appName + "-readiness")
                .withData("environment", environment);

        boolean mongoOk = checkMongo(builder);

        return mongoOk ? builder.up().build() : builder.down().build();
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
}
