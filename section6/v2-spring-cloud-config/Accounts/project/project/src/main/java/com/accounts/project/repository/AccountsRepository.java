package com.accounts.project.repository;

import com.accounts.project.entity.Accounts;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountsRepository extends JpaRepository<Accounts,Long> {
    Optional<Accounts> findByCustomerId(Long CustomerId);

    @Transactional
    @Modifying
    void deleteByCustomerId(Long CustomerId);

}
