package com.accounts.project.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;


@Schema(
        name = "Accounts",
        description = "It contains all the accounts information"
)
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Accounts extends BaseEntity{
    /*
  `customer_id` int NOT NULL,
   `account_number` int AUTO_INCREMENT  PRIMARY KEY,
  `account_type` varchar(100) NOT NULL,
  `branch_address` varchar(200) NOT NULL,
*/
    @Schema(
            description = "customerId i unique id assigned to each customer",example = "88888879"
    )
    private Long customerId;

    @Schema(
            description = "it is unique account number assigned to each account holder",example = "88888879990"
    )
    @Id
    private Long accountNumber;
    @Schema(
            description = "account type can be savings or salary etc",example = "Savings"
    )
   private String accountType;
    @Schema(
            description = "bank address",example = "Noida"
    )
   private String branchAddress;


}
