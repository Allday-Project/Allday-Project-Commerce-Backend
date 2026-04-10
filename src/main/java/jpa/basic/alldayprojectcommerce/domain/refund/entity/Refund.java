package jpa.basic.alldayprojectcommerce.domain.refund.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="order_id",nullable = false,unique = true)
    private Long orderId;

    @Column(name = "payment_uid", nullable = false, unique = true,length = 100)
    private String paymentUid;
    // 환불 사유
    @Column(nullable = false,length = 100)
    private String reason;

    // 환불 상태 (요청/완료/실패)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus refundStatus;

    private LocalDateTime requestedAt;
    private LocalDateTime refundedAt;
    private LocalDateTime failedAt;

    @Column(length = 100)
    private String failureReason;





}
