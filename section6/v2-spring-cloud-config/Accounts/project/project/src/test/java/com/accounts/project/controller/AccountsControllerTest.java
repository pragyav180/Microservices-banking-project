package com.accounts.project.controller;

import com.accounts.project.dto.AccountsContactInfoDto;
import com.accounts.project.dto.AccountsDto;
import com.accounts.project.dto.CustomerDto;
import com.accounts.project.exception.ResourceNotFound;
import com.accounts.project.service.IAccountsServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link AccountsController#fetchAccountDetail(String)} (GET /api/fetch).
 */
@WebMvcTest(AccountsController.class)
class AccountsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IAccountsServices iAccountsServices;

    // Not exercised by /api/fetch; required only so the controller bean can be constructed,
    // since AccountsContactInfoDto is registered via @EnableConfigurationProperties on
    // ProjectApplication and is therefore absent from a @WebMvcTest slice.
    @MockBean
    private AccountsContactInfoDto accountsContactInfoDto;

    // ProjectApplication carries @EnableJpaAuditing; in a @WebMvcTest slice (no entities/
    // EntityManagerFactory registered) its auto-configured "jpaMappingContext" bean fails to
    // build ("JPA metamodel must not be empty"). Mocking it here is test-only scaffolding to
    // work around that slice/auditing interaction, not a change to production code.
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    // @EnableJpaAuditing(auditorAwareRef = "auditAwareImpl") on ProjectApplication requires a
    // bean named exactly "auditAwareImpl" to be present; AuditAwareImpl (@Component) isn't
    // component-scanned into a @WebMvcTest slice, so it's supplied here by name for the same
    // reason as the mock above — test scaffolding only, no production code change.
    @MockBean(name = "auditAwareImpl")
    private AuditorAware auditAwareImpl;

    private CustomerDto buildCustomerDto(String mobileNumber) {
        CustomerDto customerDto = new CustomerDto();
        customerDto.setName("Pragya Verma");
        customerDto.setEmail("pragya@example.com");
        customerDto.setMobileNumber(mobileNumber);

        AccountsDto accountsDto = new AccountsDto();
        accountsDto.setAccountNumber(1234567890L);
        accountsDto.setAccountType("Savings");
        accountsDto.setBranchAddress("123 Main Street, New York");
        customerDto.setAccountsDto(accountsDto);

        return customerDto;
    }

    @Test
    void fetchAccountDetail_validMobileNumber_returnsOkWithCustomerDetails() throws Exception {
        String mobileNumber = "9876543210";
        when(iAccountsServices.fetchCustomer(mobileNumber)).thenReturn(buildCustomerDto(mobileNumber));

        mockMvc.perform(get("/api/fetch").param("mobileNumber", mobileNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pragya Verma"))
                .andExpect(jsonPath("$.email").value("pragya@example.com"))
                .andExpect(jsonPath("$.mobileNumber").value(mobileNumber))
                .andExpect(jsonPath("$.accountsDto.accountNumber").value(1234567890L))
                .andExpect(jsonPath("$.accountsDto.accountType").value("Savings"))
                .andExpect(jsonPath("$.accountsDto.branchAddress").value("123 Main Street, New York"));

        verify(iAccountsServices, times(1)).fetchCustomer(mobileNumber);
    }

    // NOTE on the three tests below: a @Pattern violation on this @Validated method parameter
    // throws jakarta.validation.ConstraintViolationException, which GlobalExceptionHandler does
    // not specifically handle. It therefore falls through to that handler's catch-all
    // @ExceptionHandler(Exception.class), which returns HTTP 500 with an ErrorResponseDto body
    // — confirmed by actually running these tests, not assumed. The service is still never
    // invoked, since the violation is raised before the controller method body executes.

    @Test
    void fetchAccountDetail_mobileNumberTooShort_isRejectedByValidation() throws Exception {
        mockMvc.perform(get("/api/fetch").param("mobileNumber", "12345"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.errorMessage")
                        .value("fetchAccountDetail.mobileNumber: Mobile number must be 10 digits"));

        verifyNoInteractions(iAccountsServices);
    }

    @Test
    void fetchAccountDetail_mobileNumberTooLong_isRejectedByValidation() throws Exception {
        mockMvc.perform(get("/api/fetch").param("mobileNumber", "123456789012"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.errorMessage")
                        .value("fetchAccountDetail.mobileNumber: Mobile number must be 10 digits"));

        verifyNoInteractions(iAccountsServices);
    }

    @Test
    void fetchAccountDetail_mobileNumberNonNumeric_isRejectedByValidation() throws Exception {
        mockMvc.perform(get("/api/fetch").param("mobileNumber", "abcdefghij"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.errorMessage")
                        .value("fetchAccountDetail.mobileNumber: Mobile number must be 10 digits"));

        verifyNoInteractions(iAccountsServices);
    }

    @Test
    void fetchAccountDetail_emptyMobileNumber_passesValidationAndCallsService() throws Exception {
        // The @Pattern regex "(^$|[0-9]{10})" explicitly permits an empty string.
        when(iAccountsServices.fetchCustomer("")).thenReturn(buildCustomerDto(""));

        mockMvc.perform(get("/api/fetch").param("mobileNumber", ""))
                .andExpect(status().isOk());

        verify(iAccountsServices, times(1)).fetchCustomer("");
    }

    @Test
    void fetchAccountDetail_missingMobileNumberParam_isRejectedBySpring() throws Exception {
        mockMvc.perform(get("/api/fetch"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(iAccountsServices);
    }

    @Test
    void fetchAccountDetail_customerNotFound_returnsNotFoundWithErrorBody() throws Exception {
        String mobileNumber = "9999999999";
        when(iAccountsServices.fetchCustomer(mobileNumber))
                .thenThrow(new ResourceNotFound("Customer", "mobileNumber", mobileNumber));

        mockMvc.perform(get("/api/fetch").param("mobileNumber", mobileNumber))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.errorMessage")
                        .value("Customer not found for mobileNumber and " + mobileNumber))
                .andExpect(jsonPath("$.apiPath").exists())
                .andExpect(jsonPath("$.errorTime").exists());

        verify(iAccountsServices, times(1)).fetchCustomer(mobileNumber);
    }
}
