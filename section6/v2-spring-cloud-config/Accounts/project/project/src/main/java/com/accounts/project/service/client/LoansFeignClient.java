package com.accounts.project.service.client;

import com.accounts.project.dto.CardsDto;
import com.accounts.project.dto.LoansDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name ="loans",fallback = LoansFallback.class)
public interface LoansFeignClient {

    @GetMapping(value = "/api/fetch",consumes = "application/json")
    public ResponseEntity<LoansDto> fetchLoansDetails(@RequestParam String mobileNumber,@RequestHeader("eazybank-correlation-id")String correlationId);
}
