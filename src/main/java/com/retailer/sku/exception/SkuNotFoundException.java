package com.retailer.sku.exception;

public class SkuNotFoundException extends RuntimeException {

    public SkuNotFoundException(String message) {
        super(message);
    }

    public SkuNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
