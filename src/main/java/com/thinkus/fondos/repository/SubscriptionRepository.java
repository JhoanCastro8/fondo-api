package com.thinkus.fondos.repository;

import com.thinkus.fondos.domain.Subscription;

import java.util.Optional;

public interface SubscriptionRepository {

    void save(Subscription subscription);
    Optional<Subscription> findByClientIdAndFundId(String clientId, String fundId);

}
