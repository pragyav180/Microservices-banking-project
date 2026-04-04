package com.accounts.project.mapper;

import com.accounts.project.dto.AccountsDto;
import com.accounts.project.entity.Accounts;

public class AccountsMapper {
    public static AccountsDto mapToAccountsDto(Accounts accounts,AccountsDto accountsDto){
        accountsDto.setAccountType(accounts.getAccountType());
        accountsDto.setAccountNumber(accounts.getAccountNumber());
        accountsDto.setBranchAddress(accounts.getBranchAddress());
        return accountsDto;
    }
    public static Accounts mapToAccounts(Accounts accounts,AccountsDto accountsDto){
        accounts.setAccountType(accountsDto.getAccountType());
        accounts.setAccountNumber(accountsDto.getAccountNumber());
        accounts.setBranchAddress(accountsDto.getBranchAddress());
        return accounts;
    }
}
