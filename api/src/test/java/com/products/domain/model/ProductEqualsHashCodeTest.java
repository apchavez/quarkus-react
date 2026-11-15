package com.products.domain.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

/**
 * Product's equals/hashCode/canEqual are Lombok-generated ({@code @Data} +
 * {@code @EqualsAndHashCode(callSuper = true)}). Each field comparison in the generated
 * code compiles to several branches (null checks, reference-equality short-circuits,
 * canEqual dispatch), so a normal "happy path" REST/use-case test never exercises both
 * sides of most of them. EqualsVerifier drives the full equals/hashCode contract
 * (reflexivity, symmetry, null-safety, subclass handling, field significance) in one
 * shot, which is what actually moves branch coverage here instead of hand-writing dozens
 * of one-field-at-a-time assertions.
 */
class ProductEqualsHashCodeTest {

    @Test
    void satisfiesEqualsHashCodeContract() {
        EqualsVerifier.forClass(Product.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("id")
                .suppress(Warning.NONFINAL_FIELDS)
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }
}
