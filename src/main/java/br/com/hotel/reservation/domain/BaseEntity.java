package br.com.hotel.reservation.domain;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;

@MappedSuperclass
@Getter
@ToString
public abstract class BaseEntity {

    @Version
    protected Integer version;

    protected ZonedDateTime createdAt;

    protected ZonedDateTime updatedAt;

    @PrePersist
    protected void prePersist() {
        createdAt = updatedAt = ZonedDateTime.now();
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = ZonedDateTime.now();
    }
}
