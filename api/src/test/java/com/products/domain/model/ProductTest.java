package com.products.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {

    @Test
    void hasStock_returnsFalse_whenStockIsNull() {
        Product product = new Product();
        product.stock = null;

        assertThat(product.hasStock()).isFalse();
    }

    @Test
    void hasStock_returnsFalse_whenStockIsZero() {
        Product product = new Product();
        product.stock = 0;

        assertThat(product.hasStock()).isFalse();
    }

    @Test
    void hasStock_returnsFalse_whenStockIsNegative() {
        Product product = new Product();
        product.stock = -1;

        assertThat(product.hasStock()).isFalse();
    }

    @Test
    void hasStock_returnsTrue_whenStockIsPositive() {
        Product product = new Product();
        product.stock = 5;

        assertThat(product.hasStock()).isTrue();
    }

    @Test
    void isAvailable_returnsFalse_whenActiveIsNull() {
        Product product = new Product();
        product.active = null;
        product.stock = 5;

        assertThat(product.isAvailable()).isFalse();
    }

    @Test
    void isAvailable_returnsFalse_whenActiveIsFalse() {
        Product product = new Product();
        product.active = false;
        product.stock = 5;

        assertThat(product.isAvailable()).isFalse();
    }

    @Test
    void isAvailable_returnsFalse_whenActiveTrueButNoStock() {
        Product product = new Product();
        product.active = true;
        product.stock = 0;

        assertThat(product.isAvailable()).isFalse();
    }

    @Test
    void isAvailable_returnsTrue_whenActiveTrueAndHasStock() {
        Product product = new Product();
        product.active = true;
        product.stock = 5;

        assertThat(product.isAvailable()).isTrue();
    }

    @Test
    void create_marksProductAsCreatedByGivenUser() {
        Product product = new Product();

        product.create("alice");

        assertThat(product.userCreated).isEqualTo("alice");
        assertThat(product.userUpdated).isEqualTo("alice");
        assertThat(product.created).isNotNull();
        assertThat(product.updated).isNotNull();
    }

    @Test
    void markCreated_defaultsUserToSystem_whenUserIsNull() {
        Product product = new Product();

        product.markCreated(null);

        assertThat(product.userCreated).isEqualTo("SYSTEM");
        assertThat(product.userUpdated).isEqualTo("SYSTEM");
    }

    @Test
    void markUpdated_setsGivenUser() {
        Product product = new Product();
        product.markCreated("alice");

        product.markUpdated("bob");

        assertThat(product.userUpdated).isEqualTo("bob");
    }

    @Test
    void markUpdated_defaultsUserToSystem_whenUserIsNull() {
        Product product = new Product();
        product.markCreated("alice");

        product.markUpdated(null);

        assertThat(product.userUpdated).isEqualTo("SYSTEM");
    }
}
