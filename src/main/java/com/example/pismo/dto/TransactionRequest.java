package com.example.pismo.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TransactionRequest(@NotNull Long accountId, @NotNull Integer operationTypeId,
                                 @NotNull BigDecimal amount) {
}
