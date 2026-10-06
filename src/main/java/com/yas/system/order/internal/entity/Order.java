package com.yas.system.order.internal.entity;

import com.yas.system.common.entity.BaseUuidEntity;
import com.yas.system.order.internal.enumeration.OrderStatus;
import com.yas.system.payment.internal.enumeration.PaymentMethod;
import com.yas.system.payment.internal.enumeration.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "tbl_order",
        indexes = {
                @Index(name = "idx_order_customer", columnList = "customer_id"),
                @Index(name = "idx_order_code", columnList = "order_code", unique = true)
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseUuidEntity {

    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @Column(name = "order_code", nullable = false, unique = true, length = 32)
    private String orderCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "shipping_fee", precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;

    @Column(name = "shipping_address", nullable = false, length = 300)
    private String shippingAddress;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String district;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 500)
    private String note;
}
