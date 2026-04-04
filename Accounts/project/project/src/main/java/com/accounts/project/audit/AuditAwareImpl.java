package com.accounts.project.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditAwareImpl")
public class AuditAwareImpl implements AuditorAware {
    @Override
    public Optional getCurrentAuditor() {
        return Optional.of("ACCOUNTS_MS");
    }
}
