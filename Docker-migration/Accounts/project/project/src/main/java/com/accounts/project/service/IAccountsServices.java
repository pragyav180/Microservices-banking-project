package com.accounts.project.service;


import com.accounts.project.dto.CustomerDto;

public interface IAccountsServices {
    void createAccount(CustomerDto customerDto);

    CustomerDto fetchCustomer(String mobileNumber);

    boolean updateAccount(CustomerDto customerDto);

    boolean deleteAccount(String mobileNumber);

}
