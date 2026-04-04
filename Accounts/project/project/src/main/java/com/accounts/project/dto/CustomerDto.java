package com.accounts.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerDto {
    @Schema(
            description = "It contains details of the customer"
    )
    @NotEmpty(message = "Name of the customer cannot be empty")
    @Size(min=5,max=30,message = "The length of the name should be between 5 to 30")
    private String name;

    @Schema(
            description = "It contains details of the customer email"
    )
    @NotEmpty(message = "email of the customer cannot be empty")
    @Email(message="email address should be a valid value")
    private String email;

    @Schema(
            description = "It contains details of the customer mobile number"
    )
    @Pattern(regexp="(^$|[0-9]{10})",message="Mobile number must be 10 digits")
    private String mobileNumber;

    AccountsDto accountsDto;

}
