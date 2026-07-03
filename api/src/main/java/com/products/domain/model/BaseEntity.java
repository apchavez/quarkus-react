package com.products.domain.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseEntity extends PanacheMongoEntity {

    public Instant created;
    public Instant updated;

    public String userCreated;
    public String userUpdated;

    public void markCreated(String user) {
        Instant now = Instant.now();

        this.created = now;
        this.updated = now;

        this.userCreated = user != null ? user : "SYSTEM";
        this.userUpdated = this.userCreated;
    }

    public void markUpdated(String user) {
        this.updated = Instant.now();
        this.userUpdated = user != null ? user : "SYSTEM";
    }
}