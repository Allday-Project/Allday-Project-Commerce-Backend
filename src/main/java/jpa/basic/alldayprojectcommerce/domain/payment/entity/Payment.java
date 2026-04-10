package jpa.basic.alldayprojectcommerce.domain.payment.entity;

import jakarta.persistence.*;
import jpa.basic.alldayprojectcommerce.domain.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Table(name="payments")
public class Payment extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name="payment_uid",nullable = false,unique = true)
    private String paymentUid;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private Long deliveryFee;

    @Column(nullable = false)
    private Long finalAmount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime paidAt;

    @Builder
    public Payment(Long orderId, String paymentUid, Long amount, Long deliveryFee, Long finalAmount, PaymentStatus status, LocalDateTime expiresAt) {
        this.orderId = orderId;
        this.paymentUid = paymentUid;
        this.amount = amount;
        this.deliveryFee = deliveryFee;
        this.finalAmount = finalAmount;
        this.status = status;
        this.expiresAt = expiresAt;
    }
}
