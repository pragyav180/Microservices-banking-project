package com.accounts.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AccountsDto {

    @Schema(
            description = "It is the unique number assigned to each account "
    )
    @NotNull(message = "Account number cannot be empty")
    @Pattern(regexp="(^$|[0-9]{10})",message="Mobile number must be 10 digits")
    private Long accountNumber;
    @Schema(
            description = "The account can be of any type like savings account or current account"
    )
    private String accountType;
    @Schema(
            description = "It contains details of branch"
    )
    private String branchAddress;


}
