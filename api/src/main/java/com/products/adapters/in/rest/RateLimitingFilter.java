package com.products.adapters.in.rest;

import io.quarkus.redis.datasource.RedisDataSource;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.mutiny.redis.client.Response;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.List;

@Provider
public class RateLimitingFilter implements ContainerRequestFilter {

    private static final Logger log = Logger.getLogger(RateLimitingFilter.class);

    static final int MAX_REQUESTS = 100;
    static final int WINDOW_SECONDS = 60;
    private static final String KEY_PREFIX = "rl:";
    private static final String TARGET_PATH_PREFIX = "/api/v1/products";
    private static final List<String> TARGET_METHODS = List.of("POST", "PUT", "DELETE");

    private static final String RATE_LIMIT_SCRIPT = """
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            return current
            """;

    @Inject
    RedisDataSource redisDataSource;

    @Context
    HttpServerRequest vertxRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();
        String normalizedPath = path.startsWith("/") ? path : "/" + path;

        if (!TARGET_METHODS.contains(method) || !normalizedPath.startsWith(TARGET_PATH_PREFIX)) {
            return;
        }

        String ip = clientIp(requestContext);
        long bucket = System.currentTimeMillis() / (WINDOW_SECONDS * 1000L);
        String key = KEY_PREFIX + ip + ":" + bucket;

        long current;
        try {
            Response response = redisDataSource.execute("EVAL", RATE_LIMIT_SCRIPT, "1", key,
                    String.valueOf(WINDOW_SECONDS));
            current = response.toLong();
        } catch (Exception e) {
            log.warn("[RATE-LIMIT] Redis no disponible (fail-open) — key '" + key + "': " + e.getMessage());
            return;
        }

        if (current > MAX_REQUESTS) {
            requestContext.abortWith(jakarta.ws.rs.core.Response.status(429)
                    .header("Retry-After", String.valueOf(WINDOW_SECONDS))
                    .build());
        }
    }

    private String clientIp(ContainerRequestContext requestContext) {
        String xForwardedFor = requestContext.getHeaderString("X-Forwarded-For");
        if (xForwardedFor != null) {
            String[] parts = xForwardedFor.split(",");
            for (int i = parts.length - 1; i >= 0; i--) {
                String candidate = parts[i].trim();
                if (!candidate.isBlank()) {
                    return candidate;
                }
            }
        }

        if (vertxRequest != null && vertxRequest.remoteAddress() != null
                && vertxRequest.remoteAddress().host() != null) {
            return vertxRequest.remoteAddress().host();
        }

        return "unknown";
    }
}
