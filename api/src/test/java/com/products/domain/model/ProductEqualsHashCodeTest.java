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
                // BaseEntity also redefines equals/hashCode (callSuper = true), so Product's
                // equals legitimately calls into a superclass that does the same - this is
                // the intentional Lombok callSuper chain, not a broken contract.
                .withRedefinedSuperclass()
                // `id` is inherited from Quarkus's PanacheMongoEntity, which does not
                // participate in Lombok's callSuper chain (PanacheMongoEntity itself has no
                // @EqualsAndHashCode), so it is never compared. That's an existing, intentional
                // characteristic of the Panache base class, not something this test should
                // flag as a bug.
                .withIgnoredFields("id")
                .suppress(Warning.NONFINAL_FIELDS)
                // Product is not final and has no further subclasses in this codebase, so
                // there is no meaningful "redefined subclass with extra fields" to hand
                // EqualsVerifier for the asymmetric-subclass check.
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }
}
