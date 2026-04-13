package jpa.basic.alldayprojectcommerce.domain.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

public record CreatePaymentRequest(
        @Getter
        @NotNull(message = "결제 금액은 필수 입력값입니다.")
        @Min(value = 1, message = "결제 금액은 1원 이상이어야 합니다.")
        Long amount,

        @Getter
        @NotNull(message = "배송비는 필수 입력값입니다.")
        @Min(value = 0, message = "배송비는 0원 이상이어야 합니다.")
        Long deliveryFee
) {
}
