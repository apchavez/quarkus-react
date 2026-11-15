package com.products.health;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReadinessCheckTest {

    private ReadinessCheck newReadinessCheck(MongoClient mongoClient) throws Exception {
        ReadinessCheck check = new ReadinessCheck();
        setField(check, "appName", "products-api");
        setField(check, "environment", "test");
        setField(check, "mongoClient", mongoClient);
        return check;
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = ReadinessCheck.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void call_returnsUp_whenMongoIsReachable() throws Exception {
        MongoClient mongoClient = mock(MongoClient.class);
        MongoDatabase database = mock(MongoDatabase.class);
        when(mongoClient.getDatabase("admin")).thenReturn(database);

        ReadinessCheck check = newReadinessCheck(mongoClient);

        HealthCheckResponse response = check.call();

        assertThat(response.getStatus()).isEqualTo(HealthCheckResponse.Status.UP);
    }

    @Test
    void call_returnsDown_whenMongoIsUnreachable() throws Exception {
        MongoClient mongoClient = mock(MongoClient.class);
        when(mongoClient.getDatabase("admin")).thenThrow(new RuntimeException("mongo unreachable"));

        ReadinessCheck check = newReadinessCheck(mongoClient);

        HealthCheckResponse response = check.call();

        assertThat(response.getStatus()).isEqualTo(HealthCheckResponse.Status.DOWN);
        assertThat(response.getData()).hasValueSatisfying(data -> assertThat(data)
                .containsEntry("mongodb", "down"));
    }
}
