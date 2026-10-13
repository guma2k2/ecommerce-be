package com.yas.system.payment.internal.enumeration;

/**
 * Represents the type of financial entry recorded in the payment ledger.
 */
public enum LedgerEntryType {
    /**
     * Initial reservation or hold placed on payment initialization.
     */
    INITIALIZATION,

    /**
     * Successful capture and settlement of authorized funds.
     */
    CAPTURE,

    /**
     * Funds returned to the customer.
     */
    REFUND,

    /**
     * Forced reversal of funds initiated by bank or payment dispute.
     */
    CHARGEBACK,

    /**
     * Transaction, processing, or gateway fee deducted.
     */
    FEE,

    /**
     * Manual or administrative correction to the ledger balance.
     */
    ADJUSTMENT
}
