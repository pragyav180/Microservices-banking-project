package com.accounts.project.repository;

import com.accounts.project.entity.Accounts;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountsRepository extends JpaRepository<Accounts,Long> {
    Optional<Accounts> findByCustomerId(Long CustomerId);

    @Transactional
    @Modifying
    void deleteByCustomerId(Long CustomerId);

    @Query("SELECT DISTINCT a.accountNumber from Accounts a where a.customerId = :customerId")
    List<Long> findDistinctAccountNumberByCustomerId(Long customerId);

    List<Accounts> findByAccountNumber(Long accountNumber);

    List<Long> findByAccountNumberNotIn(Collection<Long> accountNumber);

    @Query("SELECT a.accountType,count(a) from Accounts a group by a.accountType")
    List<Object[]> countEachAccountType();

    @Transactional
    @Modifying
    @Query("UPDATE Accounts a SET a.accountType = :accountType where a.customerId = :customerId")
    int updateAccountTypeByCustomerId(@Param("accountType")String accountType,@Param("customerId")Long customerId);







}
