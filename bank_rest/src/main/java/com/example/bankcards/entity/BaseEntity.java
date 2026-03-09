package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public Boolean isActive(){
        return active && !deleted;
    }

    @PreUpdate
    public void onUpdate(){
        updatedAt = LocalDateTime.now();
    }

    public void softDelete(){
        this.active = false;
        this.deleted = true;
    }
}
