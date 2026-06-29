package com.products.exception;

public class DuplicateSkuException extends RuntimeException {

    public DuplicateSkuException() {
        super("Duplicate key conflict");
    }
}
