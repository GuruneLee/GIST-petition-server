package com.gistpetition.api.petition.domain;

import com.gistpetition.api.common.persistence.UnmodifiableEntity;
import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Entity
public class Agreement extends UnmodifiableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;

    protected Agreement() {
    }

    public Agreement(Long userId) {
        this(null, userId);
    }

    private Agreement(Long id, Long userId) {
        this.id = id;
        this.userId = userId;
    }

    public boolean isAgreedBy(Long userId) {
        return this.userId.equals(userId);
    }
}
