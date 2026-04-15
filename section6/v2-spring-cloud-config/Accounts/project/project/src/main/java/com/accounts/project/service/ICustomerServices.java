package com.accounts.project.service;

import com.accounts.project.dto.CustomerDetailsDto;

public interface ICustomerServices {

    CustomerDetailsDto fetchCustomerDetailsDto(String mobileNumber);
}
