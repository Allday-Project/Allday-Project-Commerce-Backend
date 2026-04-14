package jpa.basic.alldayprojectcommerce.domain.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

public record CreatePaymentRequest(
        @NotNull(message = "결제 금액은 필수 값입니다.")
        @Min(value = 0, message = "결제 금액은 음수가 될 수 없습니다.")
        Long amount,

        @NotNull(message = "배송비는 필수 값입니다.")
        @Min(value = 0, message = "배송비는 음수가 될 수 없습니다.")
        Long deliveryFee
) {
}
