package com.accounts.project.controller;

import com.accounts.project.constants.AccountsConstants;
import com.accounts.project.dto.AccountsContactInfoDto;
import com.accounts.project.dto.CustomerDto;
import com.accounts.project.dto.ResponseDto;
import com.accounts.project.service.IAccountsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Accounts Controller",
        description = "Accounts controller that has uri to perform all CRUD operations on accounts entity"
)
@RestController
@RequestMapping(path="/api",produces = {MediaType.APPLICATION_JSON_VALUE})//produces is important to be mentioned when multiple response type are supported
//@AllArgsConstructor
@Validated
public class AccountsController {

    private final IAccountsServices iAccountsServices;

    @Autowired
    public  AccountsController(IAccountsServices iAccountsServices){
        this.iAccountsServices = iAccountsServices;
    }

    @Value("${build.version}")
    private String buildVersion;

    @Autowired
    private Environment environment;

    @Autowired
    private AccountsContactInfoDto accountsContactInfoDto;



    @Operation(
            summary = "Post API to add details to account database"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Account created successfully"
    )
    @PostMapping("/create")
    public ResponseEntity<ResponseDto> createAccount(@Valid @RequestBody CustomerDto customerDto){
        iAccountsServices.createAccount(customerDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto(AccountsConstants.STATUS_201,AccountsConstants.MESSAGE_201));
    }

    @Operation(
            summary = "Get API to fetch account details"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Success Response"
    )
    @GetMapping("/fetch")
    public ResponseEntity<CustomerDto> fetchAccountDetail(@RequestParam
                                                              @Pattern(regexp="(^$|[0-9]{10})",message="Mobile number must be 10 digits")
                                                              String mobileNumber){

       CustomerDto customerDto = iAccountsServices.fetchCustomer(mobileNumber);
               return ResponseEntity.status(HttpStatus.OK)
                       .body(customerDto);

    }

    @Operation(
            summary = "PUT API to update account details"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Request processed successfully"
    )
    @PutMapping("/update")
    public ResponseEntity<ResponseDto> updateAccountDetails(@Valid@RequestBody CustomerDto customerDto){
        boolean isUpdate = iAccountsServices.updateAccount(customerDto);
        if(isUpdate){
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseDto(AccountsConstants.STATUS_200,AccountsConstants.MESSAGE_200));
        }else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto(AccountsConstants.STATUS_500,AccountsConstants.MESSAGE_500));
        }
    }

    @Operation(
            summary = "Delete API to delete account details"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Request processed successfully"
    )
    @DeleteMapping("/delete")
    public ResponseEntity<ResponseDto> deleteAccountDetails(@RequestParam
                                                                @Pattern(regexp="(^$|[0-9]{10})",message="Mobile number must be 10 digits")
                                                                String mobileNumber){
        boolean isDeleted = iAccountsServices.deleteAccount(mobileNumber);
        if(isDeleted){
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDto(AccountsConstants.STATUS_200,AccountsConstants.MESSAGE_200));
        }
        else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto(AccountsConstants.STATUS_500,AccountsConstants.MESSAGE_500));
        }

    }

    @Operation(
            summary = "Get build information"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Success Response"
    )
    @GetMapping("/buildInfo")
    public ResponseEntity<String> getBuildInfo(){
       return ResponseEntity.status(HttpStatus.OK)
               .body(buildVersion);
    }

    @Operation(
            summary = "Get maven information"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Success Response"
    )
    @GetMapping("/java-version")
    public ResponseEntity<String> getJavaVersion(){
        return ResponseEntity.status(HttpStatus.OK)
                .body(environment.getProperty("MAVEN_HOME"));
    }

    @Operation(
            summary = "Get maven information"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Success Response"
    )
    @GetMapping("/contact-info")
    public ResponseEntity<AccountsContactInfoDto> getContactInfo(){
        return ResponseEntity.status(HttpStatus.OK)
                .body(accountsContactInfoDto);
    }


}
