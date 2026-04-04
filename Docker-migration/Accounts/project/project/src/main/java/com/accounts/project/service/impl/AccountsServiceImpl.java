package com.accounts.project.service.impl;

import com.accounts.project.constants.AccountsConstants;
import com.accounts.project.dto.AccountsDto;
import com.accounts.project.dto.CustomerDto;
import com.accounts.project.entity.Accounts;
import com.accounts.project.entity.Customer;
import com.accounts.project.exception.CustomerAlreadyExists;
import com.accounts.project.exception.ResourceNotFound;
import com.accounts.project.mapper.AccountsMapper;
import com.accounts.project.mapper.CustomerMapper;
import com.accounts.project.repository.AccountsRepository;
import com.accounts.project.repository.CustomerRepository;
import com.accounts.project.service.IAccountsServices;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class AccountsServiceImpl implements IAccountsServices {

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;
    @Override
    public void createAccount(CustomerDto customerDto) {

        Customer customer = CustomerMapper.mapToCustomer(customerDto,new Customer());
        Optional<Customer> customer1 = customerRepository.findByMobileNumber(customerDto.getMobileNumber());
        if(customer1.isPresent()){
            throw new CustomerAlreadyExists("Customer with mobile number " +customerDto.getMobileNumber()+" already exists");
        }
        Customer savedCustomer = customerRepository.save(customer);
        accountsRepository.save(createNewAccount(savedCustomer));

    }



    private Accounts createNewAccount(Customer customer) {
        Accounts newAccount = new Accounts();
        newAccount.setCustomerId(customer.getCustomerId());
        long randomAccNumber = 1000000000L + new Random().nextInt(900000000);

        newAccount.setAccountNumber(randomAccNumber);
        newAccount.setAccountType(AccountsConstants.SAVINGS);
        newAccount.setBranchAddress(AccountsConstants.ADDRESS);
        return newAccount;
    }

    @Override
    public CustomerDto fetchCustomer(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                ()-> new ResourceNotFound("Customer","mobileNumber",mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                ()-> new ResourceNotFound("Customer","mobileNumber",mobileNumber)
        );
        CustomerDto customerDto = CustomerMapper.mapToCustomerDto(customer,new CustomerDto());
        AccountsDto accountsDto = AccountsMapper.mapToAccountsDto(accounts,new AccountsDto());
        customerDto.setAccountsDto(accountsDto);
        return customerDto;

    }

    @Override
    public boolean updateAccount(CustomerDto customerDto) {
        boolean isUpdated = false;
        AccountsDto accountsDto = customerDto.getAccountsDto();
        if(accountsDto !=null ){
            Accounts accounts = accountsRepository.findById(accountsDto.getAccountNumber()).orElseThrow(
                    () -> new ResourceNotFound("Account", "AccountNumber", accountsDto.getAccountNumber().toString())
            );
            AccountsMapper.mapToAccounts( accounts,accountsDto);
            accounts = accountsRepository.save(accounts);

            Long customerId = accounts.getCustomerId();
            Customer customer = customerRepository.findById(customerId).orElseThrow(
                    () -> new ResourceNotFound("Customer", "CustomerID", customerId.toString())
            );
            CustomerMapper.mapToCustomer(customerDto,customer);
            customerRepository.save(customer);
            isUpdated = true;
        }
        return  isUpdated;
    }

    @Override
    public boolean deleteAccount(String mobileNumber) {
        boolean isDeleted = false;
        Customer customerOptional = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFound("Customer","mobileNumber",mobileNumber)
        );
        if(null!=customerOptional){
            customerRepository.deleteById(customerOptional.getCustomerId());
            accountsRepository.deleteByCustomerId(customerOptional.getCustomerId());
            isDeleted = true;
        }
        return isDeleted;
    }


}
