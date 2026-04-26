package com.ims.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public static InsufficientStockException forProduct(String sku, int requested, int available) {
        return new InsufficientStockException(
                "Insufficient stock for product '" + sku + "'. Requested: " + requested
                        + ", Available: " + available);
    }
}
