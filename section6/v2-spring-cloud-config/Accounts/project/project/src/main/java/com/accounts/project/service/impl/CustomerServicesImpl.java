package com.accounts.project.service.impl;

import com.accounts.project.dto.AccountsDto;
import com.accounts.project.dto.CardsDto;
import com.accounts.project.dto.CustomerDetailsDto;
import com.accounts.project.dto.LoansDto;
import com.accounts.project.entity.Accounts;
import com.accounts.project.entity.Customer;
import com.accounts.project.exception.ResourceNotFound;
import com.accounts.project.mapper.AccountsMapper;
import com.accounts.project.mapper.CustomerMapper;
import com.accounts.project.repository.AccountsRepository;
import com.accounts.project.repository.CustomerRepository;
import com.accounts.project.service.ICustomerServices;
import com.accounts.project.service.client.CardsFeignClient;
import com.accounts.project.service.client.LoansFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomerServicesImpl implements ICustomerServices {

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;
    private CardsFeignClient cardsFeignClient;
    private LoansFeignClient loansFeignClient;
    @Override
    public CustomerDetailsDto fetchCustomerDetailsDto(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                ()-> new ResourceNotFound("Customer","mobileNumber",mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                ()-> new ResourceNotFound("Customer","mobileNumber",mobileNumber)
        );
        CustomerDetailsDto customerDetailsDto = CustomerMapper.mapToCustomerDetailsDto(customer,new CustomerDetailsDto());
        customerDetailsDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts,new AccountsDto()));
        ResponseEntity<CardsDto> cardsDtoResponseEntity = cardsFeignClient.fetchCardDetails(mobileNumber);
        ResponseEntity<LoansDto> loansDtoResponseEntity = loansFeignClient.fetchLoansDetails(mobileNumber);
        customerDetailsDto.setCardsDto(cardsDtoResponseEntity.getBody());
        customerDetailsDto.setLoansDto(loansDtoResponseEntity.getBody());
        return customerDetailsDto;
    }
}
