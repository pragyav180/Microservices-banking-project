package com.accounts.project.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter@Setter@ToString
public class BaseEntity {

    @Schema(
            description = "It contains time at which account is created"
    )
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @Schema(
            description = "It contains details of person who has created the account"
    )
    @CreatedBy
    @Column
    private String createdBy;
    @Schema(
            description = "It contains time at which the account was updated"
    )
    @LastModifiedDate
    @Column(updatable = false)
    private LocalDateTime updatedAt;

    @Schema(
            description = "It contains details of the person who has updated the account"
    )
    @LastModifiedBy
    @Column(insertable = false)
    private String updatedBy;
}
