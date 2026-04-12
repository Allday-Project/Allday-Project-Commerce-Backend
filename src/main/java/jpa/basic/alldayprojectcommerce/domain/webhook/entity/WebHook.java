package jpa.basic.alldayprojectcommerce.domain.webhook.entity;

import jakarta.persistence.*;
import jpa.basic.alldayprojectcommerce.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "webhooks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WebHook extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String webhookId;

    @Column(nullable = false, length = 100)
    private String paymentUid;

    @Column(nullable = false)
    private String signature;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawPayload;

    @Column(nullable = false, length = 100)
    private WebhookStatus webhookStatus;

    private LocalDateTime processedAt;
    private LocalDateTime receivedAt;

    @Builder
    public WebHook(String webhookId, String paymentUid, String signature, String eventType, String rawPayload, WebhookStatus webhookStatus, LocalDateTime processedAt, LocalDateTime receivedAt) {
        this.webhookId = webhookId;
        this.paymentUid = paymentUid;
        this.signature = signature;
        this.eventType = eventType;
        this.rawPayload = rawPayload;
        this.webhookStatus = webhookStatus;
        this.processedAt = processedAt;
        this.receivedAt = receivedAt;
    }
}
