package com.yas.system.payment.internal.enumeration;

/**
 * Represents the accounting balance direction (debit or credit) for ledger entries.
 */
public enum BalanceType {
    /**
     * Entry representing an outgoing charge or decrease in available balance.
     */
    DEBIT,

    /**
     * Entry representing an incoming payment or increase in balance.
     */
    CREDIT
}
