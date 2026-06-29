package com.products.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException() {
        super("Information not found");
    }
}
