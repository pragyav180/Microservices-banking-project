package com.accounts.project.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Schema(
        name = "Customer",
        description = "Contains details of customers"
)
@Entity
@Getter@Setter@ToString@AllArgsConstructor@NoArgsConstructor
public class Customer extends BaseEntity{
    /*
  `customer_id` int AUTO_INCREMENT  PRIMARY KEY,
  `name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `mobile_number` varchar(20) NOT NULL,
*/

    @Schema(
            description = "Customer Id is the unique id assigned to each customer"
    )
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long customerId;

    @Schema(
            description = "This field contains name of the customer"
    )
    private String name;
    @Schema(
            description = "It is for storing email of the customer"
    )
    private String email;
    @Schema(
            description = "It is for storing mobile number of the customer"
    )
    private String mobileNumber;


}
