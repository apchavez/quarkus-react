package com.products.domain.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

/**
 * BaseEntity's equals/hashCode/canEqual are Lombok-generated ({@code @Data} +
 * {@code @EqualsAndHashCode(callSuper = true)}) over its four audit fields. As with
 * {@link ProductEqualsHashCodeTest}, EqualsVerifier exercises the full contract (and every
 * generated branch: null checks, reference-equality short-circuits, canEqual dispatch)
 * rather than a handful of hand-picked assertions.
 */
class BaseEntityEqualsHashCodeTest {

    @Test
    void satisfiesEqualsHashCodeContract() {
        EqualsVerifier.forClass(BaseEntity.class)
                // BaseEntity is abstract; EqualsVerifier instantiates it via a generated
                // subclass. Its immediate superclass (Quarkus's PanacheMongoEntity) does not
                // itself redefine equals/hashCode, so no withRedefinedSuperclass() is needed
                // here (only Product, whose direct superclass is BaseEntity, needs it).
                // `id` is inherited from PanacheMongoEntity and, like on Product, never
                // participates in the generated equals/hashCode.
                .withIgnoredFields("id")
                .suppress(Warning.NONFINAL_FIELDS)
                // BaseEntity is not final and isn't meant to be further subclassed with
                // additional state beyond Product (which is verified separately, including
                // its canEqual/subclass behaviour). Without an example "redefined subclass"
                // that adds new fields, EqualsVerifier can't itself prove the asymmetric
                // case, so this documents that as an accepted, intentional limitation.
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }
}
