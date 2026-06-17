package com.qnectdk.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ReissueRequest(

        @NotBlank
        String refreshToken
) {
}
