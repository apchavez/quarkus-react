package com.products.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class ReadinessCheck implements HealthCheck {

    @ConfigProperty(name = "app.name", defaultValue = "products-api")
    String appName;

    @ConfigProperty(name = "app.environment", defaultValue = "local")
    String environment;

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named(appName + "-readiness")
                .up()
                .withData("environment", environment)
                .withData("status", "Application is ready to receive requests")
                .build();
    }
}