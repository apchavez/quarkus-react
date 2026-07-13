package com.products.domain.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/*
 * callSuper is intentionally omitted: PanacheMongoEntity/PanacheMongoEntityBase (and, above
 * them, Object) do not define a meaningful field-based equals()/hashCode(). Setting
 * callSuper = true here would make Lombok chain into Object.equals()/hashCode(), which are
 * identity-based - that would make equals() always return false for any two distinct
 * instances (even with identical fields) and make hashCode() vary per-instance, silently
 * breaking the equals/hashCode contract for this class and every subclass (e.g. Product).
 */
@Data
@EqualsAndHashCode(callSuper = false)
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