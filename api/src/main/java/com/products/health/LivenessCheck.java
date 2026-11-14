package com.products.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

@Liveness
@ApplicationScoped
public class LivenessCheck implements HealthCheck {

    @ConfigProperty(name = "app.name", defaultValue = "products-api")
    String appName;

    @ConfigProperty(name = "app.version", defaultValue = "unknown")
    String version;

    @ConfigProperty(name = "app.environment", defaultValue = "local")
    String environment;

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named(appName + "-liveness")
                .up()
                .withData("version", version)
                .withData("environment", environment)
                .build();
    }
}