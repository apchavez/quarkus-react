package com.products.adapters.in.rest;

import io.quarkus.redis.datasource.RedisDataSource;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;
import io.vertx.mutiny.redis.client.Response;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RateLimitingFilterTest {

    private RateLimitingFilter filter;
    private RedisDataSource redisDataSource;
    private HttpServerRequest vertxRequest;

    @BeforeEach
    void setUp() {
        filter = new RateLimitingFilter();
        redisDataSource = mock(RedisDataSource.class);
        vertxRequest = mock(HttpServerRequest.class);
        filter.redisDataSource = redisDataSource;
        filter.vertxRequest = vertxRequest;
    }

    private ContainerRequestContext mockRequest(String method, String path, String xForwardedFor) {
        ContainerRequestContext ctx = mock(ContainerRequestContext.class);
        UriInfo uriInfo = mock(UriInfo.class);
        when(ctx.getMethod()).thenReturn(method);
        when(uriInfo.getPath()).thenReturn(path);
        when(ctx.getUriInfo()).thenReturn(uriInfo);
        when(ctx.getHeaderString("X-Forwarded-For")).thenReturn(xForwardedFor);
        return ctx;
    }

    private void stubRedisCount(long count) {
        Response response = mock(Response.class);
        when(response.toLong()).thenReturn(count);
        when(redisDataSource.execute(eq("EVAL"), any(), any(), any(), any())).thenReturn(response);
    }

    @Test
    void getRequests_shouldBypassRateLimiting() {
        ContainerRequestContext ctx = mockRequest("GET", "/api/v1/products", "1.2.3.4");

        filter.filter(ctx);

        verifyNoInteractions(redisDataSource);
        verify(ctx, never()).abortWith(any());
    }

    @Test
    void postRequest_withinLimit_shouldPassThrough() {
        stubRedisCount(1);
        ContainerRequestContext ctx = mockRequest("POST", "/api/v1/products", "1.2.3.4");

        filter.filter(ctx);

        verify(ctx, never()).abortWith(any());
    }

    @Test
    void postRequest_exceedingLimit_shouldReturn429WithRetryAfter() {
        stubRedisCount(RateLimitingFilter.MAX_REQUESTS + 1);
        ContainerRequestContext ctx = mockRequest("POST", "/api/v1/products", "1.2.3.4");

        filter.filter(ctx);

        ArgumentCaptor<jakarta.ws.rs.core.Response> captor =
                ArgumentCaptor.forClass(jakarta.ws.rs.core.Response.class);
        verify(ctx).abortWith(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(429);
        assertThat(captor.getValue().getHeaderString("Retry-After"))
                .isEqualTo(String.valueOf(RateLimitingFilter.WINDOW_SECONDS));
    }

    @Test
    void putAndDelete_alsoRateLimited() {
        stubRedisCount(RateLimitingFilter.MAX_REQUESTS + 1);

        filter.filter(mockRequest("PUT", "/api/v1/products/abc", "1.2.3.4"));
        filter.filter(mockRequest("DELETE", "/api/v1/products/abc", "1.2.3.4"));

        verify(redisDataSource, org.mockito.Mockito.times(2))
                .execute(eq("EVAL"), any(), any(), any(), any());
    }

    @Test
    void differentIps_useIsolatedKeys() {
        stubRedisCount(1);

        filter.filter(mockRequest("POST", "/api/v1/products", "1.1.1.1"));
        filter.filter(mockRequest("POST", "/api/v1/products", "2.2.2.2"));

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisDataSource, org.mockito.Mockito.times(2))
                .execute(eq("EVAL"), any(), any(), keyCaptor.capture(), any());

        assertThat(keyCaptor.getAllValues().get(0)).contains("1.1.1.1");
        assertThat(keyCaptor.getAllValues().get(1)).contains("2.2.2.2");
        assertThat(keyCaptor.getAllValues().get(0)).isNotEqualTo(keyCaptor.getAllValues().get(1));
    }

    @Test
    void redisFailure_shouldFailOpen() {
        when(redisDataSource.execute(eq("EVAL"), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Redis unavailable"));
        ContainerRequestContext ctx = mockRequest("POST", "/api/v1/products", "1.2.3.4");

        filter.filter(ctx);

        verify(ctx, never()).abortWith(any());
    }

    @Test
    void nonTargetPath_shouldBypassRateLimiting() {
        ContainerRequestContext ctx = mockRequest("POST", "/api/v1/other-resource", "1.2.3.4");

        filter.filter(ctx);

        verifyNoInteractions(redisDataSource);
    }

    @Test
    void blankForwardedForHeader_fallsBackToRemoteAddress() {
        stubRedisCount(1);
        SocketAddress socketAddress = mock(SocketAddress.class);
        when(socketAddress.host()).thenReturn("9.9.9.9");
        when(vertxRequest.remoteAddress()).thenReturn(socketAddress);
        ContainerRequestContext ctx = mockRequest("POST", "/api/v1/products", "   ");

        filter.filter(ctx);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisDataSource).execute(eq("EVAL"), any(), any(), keyCaptor.capture(), any());
        assertThat(keyCaptor.getValue()).contains("9.9.9.9");
    }

    @Test
    void multiHopForwardedFor_usesRightmostIp() {
        stubRedisCount(1);
        ContainerRequestContext ctx = mockRequest("POST", "/api/v1/products", "3.3.3.3, 4.4.4.4, 5.5.5.5");

        filter.filter(ctx);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisDataSource).execute(eq("EVAL"), any(), any(), keyCaptor.capture(), any());
        assertThat(keyCaptor.getValue()).contains("5.5.5.5");
        assertThat(keyCaptor.getValue()).doesNotContain("3.3.3.3");
    }

    @Test
    void noForwardedForAndNoRemoteAddress_resolvesToUnknown() {
        stubRedisCount(1);
        when(vertxRequest.remoteAddress()).thenReturn(null);
        ContainerRequestContext ctx = mockRequest("POST", "/api/v1/products", null);

        filter.filter(ctx);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisDataSource).execute(eq("EVAL"), any(), any(), keyCaptor.capture(), any());
        assertThat(keyCaptor.getValue()).contains("unknown");
    }
}
