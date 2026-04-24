package jpa.basic.alldayprojectcommerce.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(
        @NotBlank(message = "메시지 내용은 비어있을 수 없습니다.")
        @Size(max = 1000, message = "메시지는 1000자를 초과할 수 없습니다.")
        String content
) {
}
