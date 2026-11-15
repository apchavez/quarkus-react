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
                .withIgnoredFields("id")
                .suppress(Warning.NONFINAL_FIELDS)
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }
}
